package io.nuls.consensus.poc.tx.processor;

import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction1;
import io.nuls.consensus.poc.protocol.util.PoConvertUtil;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.consensus.poc.storage.service.PunishLogStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Niels
 * @date 2018/5/14
 */
@Component
public class RedPunishTxProcessor implements TransactionProcessor<RedPunishTransaction1> {

    @Autowired
    private PunishLogStorageService storageService;

    @Autowired
    private AgentStorageService agentStorageService;

    @Autowired
    private DepositStorageService depositStorageService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Override
    public Result onRollback(RedPunishTransaction1 tx, Object secondaryData) {
        RedPunishData punishData = tx.getTxData();

        List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        Agent agent = null;
        for (Agent agent_ : agentList) {
            if (agent_.getDelHeight() <= 0) {
                continue;
            }
            if (Arrays.equals(agent_.getAgentAddress(), punishData.getAddress())) {
                agent = agent_;
                break;
            }
        }
        if (null == agent) {
            return Result.getFailed(KernelErrorCode.DATA_ERROR, "There is no agent can be punished.");
        }
        CreateAgentTransaction transaction = (CreateAgentTransaction) this.ledgerService.getTx(agent.getTxHash());
        if (null == transaction) {
            return Result.getFailed(KernelErrorCode.DATA_ERROR, "Can't find the transaction which create the agent!");
        }
        try {
            Result rollbackResult = this.ledgerService.rollbackUnlockTxCoinData(transaction);
            if (rollbackResult.isFailed()) {
                return rollbackResult;
            }
            rollbackResult = this.accountLedgerService.rollbackUnlockTxCoinData(transaction);
            if (rollbackResult.isFailed()) {
                this.ledgerService.rollbackUnlockTxCoinData(transaction);
                return rollbackResult;
            }
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode(), e.getMessage());
        }
        List<DepositPo> depositPoList = depositStorageService.getList();
        if (null == depositPoList) {
            return Result.getSuccess();
        }
        List<Transaction> rollbackedList = new ArrayList<>();
        rollbackedList.add(transaction);
        for (DepositPo po : depositPoList) {
            if (po.getDelHeight() >= 0) {
                continue;
            }
            if (!po.getAgentHash().equals(agent.getTxHash())) {
                continue;
            }
            po.setDelHeight(-1L);
            Transaction depositTx = ledgerService.getTx(po.getTxHash());
            try {
                Result result = ledgerService.rollbackUnlockTxCoinData(depositTx);
                if (result.isFailed()) {
                    this.unlockTxList(rollbackedList,tx.getBlockHeight());
                    return result;
                }
                result = accountLedgerService.rollbackUnlockTxCoinData(depositTx);
                if (result.isFailed()) {
                    this.unlockTxList(rollbackedList,tx.getBlockHeight());
                    return result;
                }
                boolean b = depositStorageService.save(po);
                if (!b) {
                    this.unlockTxList(rollbackedList,tx.getBlockHeight());
                    return ValidateResult.getFailedResult(this.getClass().getName(), "update deposit failed!");
                }
                rollbackedList.add(depositTx);
            } catch (NulsException e) {
                this.unlockTxList(rollbackedList,tx.getBlockHeight());
                return Result.getFailed(e.getMessage());
            }
        }
        AgentPo agentPo = PoConvertUtil.agentToPo(agent);
        agentPo.setDelHeight(-1L);
        boolean success = agentStorageService.save(agentPo);
        if (!success) {
            this.unlockTxList(rollbackedList,tx.getBlockHeight());
            return Result.getFailed(KernelErrorCode.DATA_ERROR, "Can't update the agent!");
        }
        success = storageService.delete(getPoKey(punishData.getAddress(), PunishType.RED.getCode(), tx.getBlockHeight()));
        if (!success) {
            this.unlockTxList(rollbackedList,tx.getBlockHeight());
            agentPo.setDelHeight(tx.getBlockHeight());
            agentStorageService.save(agentPo);
            throw new NulsRuntimeException(KernelErrorCode.FAILED, "rollbackTransaction tx failed!");
        }

        return Result.getSuccess();
    }

    private void unlockTxList(List<Transaction> rollbackedList,long height) {
        for (Transaction depositTx : rollbackedList) {
            try {
                ledgerService.unlockTxCoinData(depositTx, height);
            } catch (NulsException e) {
                Log.error(e);
            }
            accountLedgerService.unlockCoinData(depositTx, height);
        }
    }

    @Override
    public Result onCommit(RedPunishTransaction1 tx, Object secondaryData) {
        RedPunishData punishData = tx.getTxData();
        BlockHeader header = (BlockHeader) secondaryData;
        BlockRoundData roundData = new BlockRoundData(header.getExtend());
        PunishLogPo punishLogPo = new PunishLogPo();
        punishLogPo.setAddress(punishData.getAddress());
        punishLogPo.setHeight(tx.getBlockHeight());
        punishLogPo.setRoundIndex(roundData.getRoundIndex());
        punishLogPo.setTime(tx.getTime());
        punishLogPo.setType(PunishType.RED.getCode());

        List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        Agent agent = null;
        for (Agent agent_ : agentList) {
            if (agent_.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(agent_.getAgentAddress(), punishLogPo.getAddress())) {
                agent = agent_;
                break;
            }
        }
        if (null == agent) {
            Log.error("There is no agent can be punished.");
            return Result.getSuccess();
        }
        CreateAgentTransaction transaction = (CreateAgentTransaction) this.ledgerService.getTx(agent.getTxHash());
        if (null == transaction) {
            return Result.getFailed(KernelErrorCode.DATA_ERROR, "Can't find the transaction which create the agent!");
        }
        try {
            Result unlockResult = this.ledgerService.unlockTxCoinData(transaction, tx.getTime() + 60 * 24 * 3600000L);
            if (unlockResult.isFailed()) {
                return unlockResult;
            }
            unlockResult = this.accountLedgerService.unlockCoinData(transaction, tx.getTime() + 60 * 24 * 3600000L);
            if (unlockResult.isFailed()) {
                this.ledgerService.rollbackUnlockTxCoinData(transaction);
                return unlockResult;
            }
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode(), e.getMessage());
        }
        List<DepositPo> depositPoList = depositStorageService.getList();
        if (null == depositPoList) {
            return Result.getSuccess();
        }
        List<Transaction> unlockedList = new ArrayList<>();
        unlockedList.add(transaction);
        for (DepositPo po : depositPoList) {
            if (po.getDelHeight() >= 0) {
                continue;
            }
            if (!po.getAgentHash().equals(agent.getTxHash())) {
                continue;
            }
            po.setDelHeight(tx.getBlockHeight());
            Transaction depositTx = ledgerService.getTx(po.getTxHash());
            try {
                Result result = ledgerService.unlockTxCoinData(depositTx, 0L);
                if (result.isFailed()) {
                    this.rollbackUnlockTxList(unlockedList);
                    return result;
                }
                result = accountLedgerService.unlockCoinData(depositTx, 0L);
                if (result.isFailed()) {
                    this.rollbackUnlockTxList(unlockedList);
                    return result;
                }
                boolean b = depositStorageService.save(po);
                if (!b) {
                    this.rollbackUnlockTxList(unlockedList);
                    return ValidateResult.getFailedResult(this.getClass().getName(), "update deposit failed!");
                }
                unlockedList.add(depositTx);
            } catch (NulsException e) {
                this.rollbackUnlockTxList(unlockedList);
                return Result.getFailed(e.getMessage());
            }
        }
        boolean success = storageService.save(punishLogPo);
        if (!success) {
            this.rollbackUnlockTxList(unlockedList);
            throw new NulsRuntimeException(KernelErrorCode.FAILED, "rollbackTransaction tx failed!");
        }
        AgentPo agentPo = PoConvertUtil.agentToPo(agent);
        agentPo.setDelHeight(tx.getBlockHeight());
        success = agentStorageService.save(agentPo);
        if (!success) {
            this.rollbackUnlockTxList(unlockedList);
            this.storageService.delete(punishLogPo.getKey());
            return Result.getFailed(KernelErrorCode.DATA_ERROR, "Can't update the agent!");
        }
        return Result.getSuccess();
    }

    private void rollbackUnlockTxList(List<Transaction> unlockedList) {
        for (Transaction depositTx : unlockedList) {
            try {
                ledgerService.rollbackUnlockTxCoinData(depositTx);
            } catch (NulsException e) {
                Log.error(e);
            }
            accountLedgerService.rollbackUnlockTxCoinData(depositTx);
        }
    }

    /**
     * 获取固定格式的key
     */
    private byte[] getPoKey(byte[] address, byte type, long blockHeight) {
        return ArraysTool.joinintTogether(address, new byte[]{type}, SerializeUtils.uint64ToByteArray(blockHeight));
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        return ValidateResult.getSuccessResult();
    }
}

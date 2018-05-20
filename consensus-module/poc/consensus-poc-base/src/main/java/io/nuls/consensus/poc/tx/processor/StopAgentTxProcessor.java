package io.nuls.consensus.poc.tx.processor;

import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2018/5/17
 */
@Component
public class StopAgentTxProcessor implements TransactionProcessor<StopAgentTransaction> {

    @Autowired
    private AgentStorageService agentStorageService;

    @Autowired
    private DepositStorageService depositStorageService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Override
    public Result onRollback(StopAgentTransaction tx, Object secondaryData) {
        AgentPo agentPo = agentStorageService.get(tx.getTxData().getCreateTxHash());
        if (null == agentPo || agentPo.getDelHeight() < 0) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_ERROR, "the agent is not exist or it's never stopped!");
        }
        agentPo.setDelHeight(-1L);

//       重新锁定所有委托到该节点的委托金额
//       locks the amount of trust that is delegated to the node.
        List<DepositPo> depositPoList = depositStorageService.getList();
        if (null == depositPoList) {
            return Result.getSuccess();
        }
        List<Transaction> rollbackedList = new ArrayList<>();
        for (DepositPo po : depositPoList) {
            if (po.getDelHeight() != tx.getBlockHeight()) {
                continue;
            }
            if (!po.getAgentHash().equals(agentPo.getHash())) {
                continue;
            }
            po.setDelHeight(-1L);
            Transaction depositTx = ledgerService.getTx(po.getTxHash());
            try {
                Result result = ledgerService.rollbackUnlockTxCoinData(depositTx);
                if (result.isFailed()) {
                    this.unlock(rollbackedList);
                    return result;
                }
                result = accountLedgerService.rollbackUnlockTxCoinData(depositTx);
                if (result.isFailed()) {
                    this.unlock(rollbackedList);
                    return result;
                }
                boolean b = depositStorageService.save(po);
                if (!b) {
                    this.unlock(rollbackedList);
                    return ValidateResult.getFailedResult(this.getClass().getName(), "update deposit failed!");
                }
                rollbackedList.add(depositTx);
            } catch (NulsException e) {
                this.rollbackUnlock(rollbackedList);
                return Result.getFailed(e.getMessage());
            }
        }

        boolean b = agentStorageService.save(agentPo);
        if (!b) {
            this.unlock(rollbackedList);
            return Result.getFailed("update agent failed!");
        }
        return Result.getSuccess();
    }

    private void unlock(List<Transaction> rollbackedList) {
        for (Transaction depositTx : rollbackedList) {
            try {
                ledgerService.unlockTxCoinData(depositTx, 0L);
                accountLedgerService.unlockCoinData(depositTx, 0L);
            } catch (NulsException e) {
                Log.error(e);
            }
        }
    }

    @Override
    public Result onCommit(StopAgentTransaction tx, Object secondaryData) {
        AgentPo agentPo = agentStorageService.get(tx.getTxData().getCreateTxHash());
        if (null == agentPo || agentPo.getDelHeight() > 0) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_ERROR, "the agent is not exist or had deleted!");
        }
        agentPo.setDelHeight(tx.getBlockHeight());
        tx.getTxData().setAddress(agentPo.getAgentAddress());
//       解锁所有委托到该节点的委托金额
//        Unlocks the amount of trust that is delegated to the node.
        List<DepositPo> depositPoList = depositStorageService.getList();
        if (null == depositPoList) {
            return Result.getSuccess();
        }
        List<Transaction> unlockedList = new ArrayList<>();
        for (DepositPo po : depositPoList) {
            if (po.getDelHeight() >= 0) {
                continue;
            }
            if (po.getBlockHeight() > tx.getBlockHeight()) {
                continue;
            }
            if (!po.getAgentHash().equals(agentPo.getHash())) {
                continue;
            }
            po.setDelHeight(tx.getBlockHeight());
            Transaction depositTx = ledgerService.getTx(po.getTxHash());
            try {
                Result result = ledgerService.unlockTxCoinData(depositTx, 0L);
                if (result.isFailed()) {
                    this.rollbackUnlock(unlockedList);
                    return result;
                }
                result = accountLedgerService.unlockCoinData(depositTx, 0L);
                if (result.isFailed()) {
                    this.rollbackUnlock(unlockedList);
                    return result;
                }
                boolean b = depositStorageService.save(po);
                if (!b) {
                    this.rollbackUnlock(unlockedList);
                    return ValidateResult.getFailedResult(this.getClass().getName(), "update deposit failed!");
                }
                unlockedList.add(depositTx);
            } catch (NulsException e) {
                this.rollbackUnlock(unlockedList);
                return Result.getFailed(e.getMessage());
            }
        }

        boolean b = agentStorageService.save(agentPo);
        if (!b) {
            this.rollbackUnlock(unlockedList);
            return Result.getFailed("update agent failed!");
        }
        return Result.getSuccess();
    }

    private void rollbackUnlock(List<Transaction> unlockedList) {
        for (Transaction depositTx : unlockedList) {
            try {
                ledgerService.rollbackUnlockTxCoinData(depositTx);
                accountLedgerService.rollbackUnlockTxCoinData(depositTx);
            } catch (NulsException e) {
                Log.error(e);
            }
        }
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        if (txList == null || txList.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        Set<NulsDigestData> hashSet = new HashSet<>();
        Set<String> addressSet = new HashSet<>();
        for (Transaction tx : txList) {
            if (tx.getType() == ConsensusConstant.TX_TYPE_RED_PUNISH) {
                RedPunishTransaction transaction = (RedPunishTransaction) tx;
                addressSet.add(Base58.encode(transaction.getTxData().getAddress()));
            } else if (tx.getType() == ConsensusConstant.TX_TYPE_STOP_AGENT) {
                StopAgentTransaction transaction = (StopAgentTransaction) tx;
                if (!hashSet.add(transaction.getTxData().getCreateTxHash())) {
                    ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), "transactions repeated!");
                    result.setData(transaction);
                    return result;
                }
                if (addressSet.contains(Base58.encode(transaction.getTxData().getAddress()))) {
                    ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), "The agent has stopped by Red Punish trnasaction!");
                    result.setData(transaction);
                    return result;
                }
            }
        }
        return ValidateResult.getSuccessResult();
    }
}

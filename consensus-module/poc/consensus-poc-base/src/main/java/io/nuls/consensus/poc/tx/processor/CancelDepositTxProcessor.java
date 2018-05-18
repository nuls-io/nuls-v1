package io.nuls.consensus.poc.tx.processor;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.tx.CancelDepositTransaction;
import io.nuls.consensus.poc.protocol.tx.DepositTransaction;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2018/5/17
 */
@Component
public class CancelDepositTxProcessor implements TransactionProcessor<CancelDepositTransaction> {

    @Autowired
    private DepositStorageService depositStorageService;

    @Autowired
    private AgentStorageService agentStorageService;

    @Autowired
    private LedgerService ledgerService;

    @Override
    public Result onRollback(CancelDepositTransaction tx, Object secondaryData) {
        DepositTransaction transaction = (DepositTransaction) ledgerService.getTx(tx.getTxData().getJoinTxHash());
        if (null == transaction) {
            return Result.getFailed("Can not find the deposit!");
        }
        DepositPo po = depositStorageService.get(tx.getTxData().getJoinTxHash());
        if (null == po) {
            return Result.getFailed("Can not find the deposit!");
        }
        if (po.getDelHeight() != tx.getBlockHeight()) {
            return Result.getFailed("The deposit never canceled before!");
        }
        po.setDelHeight(-1L);
        boolean b = depositStorageService.save(po);
        if (b) {
            return Result.getSuccess();
        }
        return Result.getFailed("Save the deposit failed!");
    }

    @Override
    public Result onCommit(CancelDepositTransaction tx, Object secondaryData) {
        DepositTransaction transaction = (DepositTransaction) ledgerService.getTx(tx.getTxData().getJoinTxHash());
        if (null == transaction) {
            return Result.getFailed("Can not find the deposit!");
        }
        DepositPo po = depositStorageService.get(tx.getTxData().getJoinTxHash());
        if (null == po) {
            return Result.getFailed("Can not find the deposit!");
        }
        if (po.getDelHeight() > 0) {
            return Result.getFailed("The deposit was canceled before!");
        }
        po.setDelHeight(tx.getBlockHeight());
        boolean b = depositStorageService.save(po);
        if (b) {
            return Result.getSuccess();
        }
        return Result.getFailed("Save the deposit failed!");
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        if (txList == null || txList.size() <= 1) {
            return ValidateResult.getSuccessResult();
        }
        Set<NulsDigestData> hashSet = new HashSet<>();
        Set<NulsDigestData> agentHashSet = new HashSet<>();
        Set<String> addressSet = new HashSet<>();

        for (Transaction tx : txList) {
            if (tx.getType() == ConsensusConstant.TX_TYPE_RED_PUNISH) {
                RedPunishTransaction transaction = (RedPunishTransaction) tx;
                addressSet.add(Base58.encode(transaction.getTxData().getAddress()));
            } else if (tx.getType() == ConsensusConstant.TX_TYPE_STOP_AGENT) {
                StopAgentTransaction transaction = (StopAgentTransaction) tx;
                agentHashSet.add(transaction.getTxData().getCreateTxHash());
            }
        }
        for (Transaction tx : txList) {
            if (tx.getType() == ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT) {
                CancelDepositTransaction transaction = (CancelDepositTransaction) tx;
                if (hashSet.contains(transaction.getTxData().getJoinTxHash())) {
                    return ValidateResult.getFailedResult(this.getClass().getName(), "transaction repeated!");
                }
                if (agentHashSet.contains(transaction.getTxData().getJoinTxHash())) {
                    return ValidateResult.getFailedResult(this.getClass().getName(), "The agent is stopped!");
                }
                DepositPo depositPo = depositStorageService.get(transaction.getTxData().getJoinTxHash());
                AgentPo agentPo = agentStorageService.get(depositPo.getAgentHash());
                if (null == agentPo) {
                    return ValidateResult.getFailedResult(this.getClass().getName(), "Can't find the agent!");
                }
                if (addressSet.contains(Base58.encode(agentPo.getAgentAddress()))) {
                    return ValidateResult.getFailedResult(this.getClass().getName(), "The agent was punished!");
                }
            }
        }

        return ValidateResult.getSuccessResult();
    }
}

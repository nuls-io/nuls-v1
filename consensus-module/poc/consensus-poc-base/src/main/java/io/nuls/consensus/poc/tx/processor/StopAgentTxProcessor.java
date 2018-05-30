package io.nuls.consensus.poc.tx.processor;

import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
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

        boolean b = agentStorageService.save(agentPo);
        if (!b) {
            return Result.getFailed("update agent failed!");
        }
        return Result.getSuccess();
    }

    @Override
    public Result onCommit(StopAgentTransaction tx, Object secondaryData) {
        AgentPo agentPo = agentStorageService.get(tx.getTxData().getCreateTxHash());
        if (null == agentPo || agentPo.getDelHeight() > 0) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_ERROR, "the agent is not exist or had deleted!");
        }
        agentPo.setDelHeight(tx.getBlockHeight());
        tx.getTxData().setAddress(agentPo.getAgentAddress());

        boolean b = agentStorageService.save(agentPo);
        if (!b) {
            return Result.getFailed("update agent failed!");
        }
        return Result.getSuccess();
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
                if (transaction.getTxData().getAddress() == null) {
                    CreateAgentTransaction agentTransaction = (CreateAgentTransaction) ledgerService.getTx(transaction.getTxData().getCreateTxHash());
                    if (null == agentTransaction) {
                        ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), "agent transaction not exist!");
                        result.setData(transaction);
                        return result;
                    }
                    transaction.getTxData().setAddress(agentTransaction.getTxData().getAgentAddress());
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

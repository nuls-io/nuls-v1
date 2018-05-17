package io.nuls.consensus.poc.tx.processor;

import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;

import java.util.ArrayList;
import java.util.List;

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
        // todo auto-generated method stub
        return null;
    }

    @Override
    public Result onCommit(StopAgentTransaction tx, Object secondaryData) {
        AgentPo agentPo = agentStorageService.get(tx.getTxData().getCreateTxHash());
        if (null == agentPo || agentPo.getDelHeight() > 0) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_ERROR, "the agent is not exist or had deleted!");
        }
        agentPo.setDelHeight(tx.getBlockHeight());

//       解锁所有委托到该节点的委托金额
//        Unlocks the amount of trust that is delegated to the node.
        List<DepositPo> depositPoList = depositStorageService.getList();
        if (null == depositPoList) {
            return Result.getSuccess();
        }
        List<Transaction> unlockedList = new ArrayList<>();
        for (DepositPo po : depositPoList) {
            po.setDelHeight(tx.getBlockHeight());
            Transaction depositTx = ledgerService.getTx(po.getTxHash());
            try {
                Result result = ledgerService.unlockTxCoinData(depositTx);
                if (result.isFailed()) {
                    this.rollbackUnlock(unlockedList);
                    return result;
                }
                result = accountLedgerService.unlockCoinData(depositTx);
                if (result.isFailed()) {
                    this.rollbackUnlock(unlockedList);
                    return result;
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
        // todo auto-generated method stub
        return null;
    }
}

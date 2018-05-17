package io.nuls.consensus.poc.tx.processor;

import io.nuls.consensus.poc.protocol.tx.CancelDepositTransaction;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.validate.ValidateResult;

import java.util.List;

/**
 * @author Niels
 * @date 2018/5/17
 */
@Component
public class CancelDepositTxProcessor implements TransactionProcessor<CancelDepositTransaction> {
    @Override
    public Result onRollback(CancelDepositTransaction tx, Object secondaryData) {
        // todo auto-generated method stub
        return null;
    }

    @Override
    public Result onCommit(CancelDepositTransaction tx, Object secondaryData) {
        // todo auto-generated method stub
        return null;
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        // todo auto-generated method stub
        return null;
    }
}

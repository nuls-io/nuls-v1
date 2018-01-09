package io.nuls.account.service.tx;

import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class AliasTxService implements TransactionService<AliasTransaction> {
    @Override
    public void onRollback(AliasTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onCommit(AliasTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onApproval(AliasTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }
}

package io.nuls.consensus.service.tx;

import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class RedPunishTxService implements TransactionService<RedPunishTransaction>{
    @Override
    public void onRollback(RedPunishTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onCommit(RedPunishTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onApproval(RedPunishTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }
}

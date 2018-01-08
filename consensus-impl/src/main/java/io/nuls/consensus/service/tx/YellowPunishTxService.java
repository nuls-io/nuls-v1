package io.nuls.consensus.service.tx;

import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class YellowPunishTxService implements TransactionService<YellowPunishTransaction>{
    @Override
    public void onRollback(YellowPunishTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onCommit(YellowPunishTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onApproval(YellowPunishTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }
}

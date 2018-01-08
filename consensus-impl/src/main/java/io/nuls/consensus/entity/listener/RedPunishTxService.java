package io.nuls.consensus.entity.listener;

import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.core.tx.serivce.TransactionService;

/**
 * @author Niels
 * @date 2017/12/28
 */
public class RedPunishTxService implements TransactionService<RedPunishTransaction> {

    private static final RedPunishTxService INSTANCE = new RedPunishTxService();

    private RedPunishTxService() {
    }

    public static RedPunishTxService getInstance() {
        return INSTANCE;
    }

    @Override
    public void onRollback(RedPunishTransaction tx) {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onCommit(RedPunishTransaction tx) {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onApproval(RedPunishTransaction tx) {
        // todo auto-generated method stub(niels)

    }
}

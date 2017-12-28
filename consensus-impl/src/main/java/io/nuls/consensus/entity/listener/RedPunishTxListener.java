package io.nuls.consensus.entity.listener;

import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.core.chain.entity.TransactionListener;

/**
 * @author Niels
 * @date 2017/12/28
 */
public class RedPunishTxListener implements TransactionListener<RedPunishTransaction> {

    private static final RedPunishTxListener INSTANCE = new RedPunishTxListener();

    private RedPunishTxListener() {
    }

    public static RedPunishTxListener getInstance() {
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

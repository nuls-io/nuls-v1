package io.nuls.consensus.entity.listener;

import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.core.tx.serivce.TransactionService;

/**
 * @author Niels
 * @date 2017/12/28
 */
public class YellowPunishTxListener implements TransactionService<YellowPunishTransaction> {

    private static final YellowPunishTxListener INSTANCE = new YellowPunishTxListener();

    private YellowPunishTxListener() {
    }

    public static YellowPunishTxListener getInstance() {
        return INSTANCE;
    }

    @Override
    public void onRollback(YellowPunishTransaction tx) {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onCommit(YellowPunishTransaction tx) {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onApproval(YellowPunishTransaction tx) {
        // todo auto-generated method stub(niels)

    }
}

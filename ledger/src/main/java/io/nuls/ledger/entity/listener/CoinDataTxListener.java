package io.nuls.ledger.entity.listener;

import io.nuls.core.chain.entity.TransactionListener;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class CoinDataTxListener implements TransactionListener<AbstractCoinTransaction> {
    private static final CoinDataTxListener INSTANCE = new CoinDataTxListener();
    private CoinDataTxListener(){}
    @Override
    public void onRollback(AbstractCoinTransaction tx) {
        tx.getCoinDataProvider().rollback(tx.getCoinData(),tx.getHash().getDigestHex());
    }

    @Override
    public void onCommit(AbstractCoinTransaction tx) {
        tx.getCoinDataProvider().save(tx.getCoinData(),tx.getHash().getDigestHex());
    }

    @Override
    public void onApproval(AbstractCoinTransaction tx) {
        tx.getCoinDataProvider().approve(tx.getCoinData(),tx.getHash().getDigestHex());
    }

    public static CoinDataTxListener getInstance() {
        return INSTANCE;
    }
}

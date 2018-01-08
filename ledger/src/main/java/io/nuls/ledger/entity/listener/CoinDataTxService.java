package io.nuls.ledger.entity.listener;

import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class CoinDataTxService implements TransactionService<AbstractCoinTransaction> {
    private static final CoinDataTxService INSTANCE = new CoinDataTxService();
    private CoinDataTxService(){}

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

    public static CoinDataTxService getInstance() {
        return INSTANCE;
    }
}

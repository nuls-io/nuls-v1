package io.nuls.consensus.handler;

import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.entity.CoinTransaction;
import io.nuls.ledger.event.CoinTransactionEvent;

/**
 * Created by facjas on 2017/11/16.
 */
public class TransactionHandler extends NetworkNulsEventHandler<CoinTransactionEvent<CoinTransaction>> {

    @Override
    public void onEvent(CoinTransactionEvent<CoinTransaction> event) throws NulsException{
        CoinTransaction tx = event.getEventBody();
        tx.verify();
    }
}

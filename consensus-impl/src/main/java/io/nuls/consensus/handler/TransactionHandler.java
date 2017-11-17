package io.nuls.consensus.handler;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.CoinTransactionEvent;

/**
 * Created by facjas on 2017/11/16.
 */
public class TransactionHandler extends NetworkNulsEventHandler<CoinTransactionEvent> {

    @Override
    public void onEvent(CoinTransactionEvent event) throws NulsException{
        Transaction tx = event.getEventBody();
        tx.verify();
    }
}

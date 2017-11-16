package io.nuls.consensus.handler;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.TransactionEvent;

/**
 * Created by facjas on 2017/11/16.
 */
public class TransactionHandler extends NetworkNulsEventHandler<TransactionEvent> {

    @Override
    public void onEvent(TransactionEvent event) throws NulsException{
        Transaction tx = event.getTransaction();
        tx.verify();
    }
}

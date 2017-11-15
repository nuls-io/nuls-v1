package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.TransactionEvent;

/**
 * Created by Niels on 2017/11/13.
 */
public class TransactionHandler extends NetworkNulsEventHandler<TransactionEvent> {
    @Override
    public void onEvent(TransactionEvent event) {
        //todo
    }
}

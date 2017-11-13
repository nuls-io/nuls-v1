package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NulsEventHandler;
import io.nuls.ledger.event.TransactionEvent;

/**
 * Created by Niels on 2017/11/13.
 * nuls.io
 */
public class TransactionHandler extends NulsEventHandler<TransactionEvent> {
    @Override
    public void onEvent(TransactionEvent event) {
        //todo
    }
}

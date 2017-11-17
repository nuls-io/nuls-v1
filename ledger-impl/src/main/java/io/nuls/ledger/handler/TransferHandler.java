package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.CoinTransactionEvent;
import io.nuls.ledger.event.TransferEvent;

/**
 * Created by Niels on 2017/11/13.
 */
public class TransferHandler extends NetworkNulsEventHandler<TransferEvent> {
    @Override
    public void onEvent(TransferEvent event) {
        //todo
    }
}

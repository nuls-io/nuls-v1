package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.CoinTransactionEvent;

/**
 * Created by Niels on 2017/11/13.
 */
public class UtxoCoinTransactionHandler extends NetworkNulsEventHandler<CoinTransactionEvent> {
    @Override
    public void onEvent(CoinTransactionEvent event) {
        //todo
    }
}

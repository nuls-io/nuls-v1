package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.UtxoSmallChangeEvent;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoSmallChangeHandler extends NetworkNulsEventHandler<UtxoSmallChangeEvent> {
    @Override
    public void onEvent(UtxoSmallChangeEvent event) {
        //todo
    }
}

package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.UtxoLockEvent;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoLockHandler extends NetworkNulsEventHandler<UtxoLockEvent> {

    @Override
    public void onEvent(UtxoLockEvent event) {
        //todo

    }
}

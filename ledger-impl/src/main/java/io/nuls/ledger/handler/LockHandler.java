package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.LockEvent;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class LockHandler extends NetworkNulsEventHandler<LockEvent> {

    @Override
    public void onEvent(LockEvent event) {
        //todo

    }
}

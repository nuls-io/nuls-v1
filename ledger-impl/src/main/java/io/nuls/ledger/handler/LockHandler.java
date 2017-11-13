package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NulsEventHandler;
import io.nuls.ledger.event.LockEvent;

/**
 * Created by Niels on 2017/11/13.
 * nuls.io
 */
public class LockHandler extends NulsEventHandler<LockEvent> {
    @Override
    public void onEvent(LockEvent event) {
        //todo
    }
}

package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NulsEventHandler;
import io.nuls.ledger.event.SmallChangeEvent;

/**
 * Created by Niels on 2017/11/13.
 * nuls.io
 */
public class SmallChangeHandler extends NulsEventHandler<SmallChangeEvent> {
    @Override
    public void onEvent(SmallChangeEvent event) {
        //todo
    }
}

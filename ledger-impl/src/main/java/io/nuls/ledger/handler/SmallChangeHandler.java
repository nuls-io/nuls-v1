package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.SmallChangeEvent;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class SmallChangeHandler extends NetworkNulsEventHandler<SmallChangeEvent> {
    @Override
    public void onEvent(SmallChangeEvent event) {
        //todo
    }
}

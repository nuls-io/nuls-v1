package io.nuls.ledger.handler;

import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.ledger.event.LockCoinEvent;

/**
 * Created by Niels on 2017/11/13.
 */
public class UnlockCoinBusHandler<T extends LockCoinEvent> extends AbstractEventBusHandler<T> {

    @Override
    public void onEvent(T event,String fromId) {
        //todo
    }
}

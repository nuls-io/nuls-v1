package io.nuls.ledger.handler;

import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;

/**
 * Created by Niels on 2017/11/13.
 */
public class UtxoDepositBusHandler<T extends io.nuls.ledger.event.LockNulsEvent> extends AbstractEventBusHandler<T> {

    @Override
    public void onEvent(T event,String fromId) {
        //todo
    }
}

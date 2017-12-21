package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.AbstractEventHandler;
import io.nuls.ledger.event.UtxoLockNulsEvent;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoLockHandler<T extends UtxoLockNulsEvent> extends AbstractEventHandler<T> {

    @Override
    public void onEvent(T event,String fromId) {
        //todo
    }
}

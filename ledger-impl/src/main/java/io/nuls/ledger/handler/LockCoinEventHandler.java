package io.nuls.ledger.handler;

import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.ledger.event.LockCoinEvent;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class LockCoinEventHandler<T extends LockCoinEvent> extends AbstractNetworkEventHandler<T> {

    @Override
    public void onEvent(T event,String fromId) {
        //todo
    }
}

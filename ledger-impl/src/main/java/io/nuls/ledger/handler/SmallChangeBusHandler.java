package io.nuls.ledger.handler;

import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.ledger.event.SmallChangeEvent;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class SmallChangeBusHandler<T extends SmallChangeEvent> extends AbstractEventBusHandler<T> {

    @Override
    public void onEvent(T event, String fromId) {
        //todo

    }
}

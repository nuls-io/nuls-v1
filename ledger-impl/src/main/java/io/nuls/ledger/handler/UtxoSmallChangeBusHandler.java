package io.nuls.ledger.handler;

import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.ledger.event.UtxoSmallChangeEvent;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class UtxoSmallChangeBusHandler<T extends UtxoSmallChangeEvent> extends AbstractEventBusHandler<T> {

    @Override
    public void onEvent(T event, String fromId) {
        //todo

    }
}

package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.AbstractEventHandler;
import io.nuls.ledger.event.UtxoSmallChangeEvent;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class UtxoSmallChangeHandler<T extends UtxoSmallChangeEvent> extends AbstractEventHandler<T> {

    @Override
    public void onEvent(T event, String fromId) {
        //todo

    }
}

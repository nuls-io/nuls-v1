package io.nuls.ledger.handler;

import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.ledger.event.AbstractCoinTransactionEvent;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class UtxoCoinTransactionBusHandler<T extends AbstractCoinTransactionEvent> extends AbstractEventBusHandler<T> {

    @Override
    public void onEvent(T event,String fromId)  {
        //todo

    }
}

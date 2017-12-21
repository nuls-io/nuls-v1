package io.nuls.ledger.handler;

import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.ledger.event.UtxoDepositNulsEvent;

/**
 * Created by Niels on 2017/11/13.
 */
public class UtxoDepositBusHandler<T extends UtxoDepositNulsEvent> extends AbstractEventBusHandler<T> {

    @Override
    public void onEvent(T event,String fromId) {
        //todo
    }
}

package io.nuls.ledger.handler;

import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.ledger.event.AbstractCoinTransactionEvent;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class UtxoCoinTransactionHandler<T extends AbstractCoinTransactionEvent> extends AbstractNetworkNulsEventHandler<T> {

    @Override
    public void onEvent(T event,String fromId)  {
        //todo

    }
}

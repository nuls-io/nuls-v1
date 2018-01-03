
package io.nuls.ledger.handler;

import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.ledger.event.AbstractCoinTransactionEvent;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class CoinTransactionEventHandler<T extends AbstractCoinTransactionEvent> extends AbstractNetworkEventHandler<T> {

    @Override
    public void onEvent(T event,String fromId)  {
        //todo

    }
}
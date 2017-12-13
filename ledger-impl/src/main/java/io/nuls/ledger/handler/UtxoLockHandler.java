package io.nuls.ledger.handler;

import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.ledger.event.UtxoLockCoinEvent;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoLockHandler<T extends UtxoLockCoinEvent> extends AbstractNetworkNulsEventHandler<T> {

    @Override
    public void onEvent(T event,String fromId) throws NulsException {
        //todo
    }
}

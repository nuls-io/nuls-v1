package io.nuls.ledger.handler;

import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.UtxoSmallChangeEvent;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoSmallChangeHandler<T extends UtxoSmallChangeEvent> extends NetworkNulsEventHandler<T> {

    @Override
    public void onEvent(T event) throws NulsException {
        //todo

    }
}

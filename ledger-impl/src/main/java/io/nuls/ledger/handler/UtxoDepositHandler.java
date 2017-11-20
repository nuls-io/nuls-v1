package io.nuls.ledger.handler;

import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.UtxoDepositCoinEvent;

/**
 * Created by Niels on 2017/11/13.
 */
public class UtxoDepositHandler<T extends UtxoDepositCoinEvent> extends NetworkNulsEventHandler<T>  {

    @Override
    public void onEvent(T event) throws NulsException {
        //todo
    }
}

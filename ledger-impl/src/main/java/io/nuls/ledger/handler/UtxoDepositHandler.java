package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.UtxoDepositEvent;
import io.nuls.ledger.event.UtxoTransferEvent;

/**
 * Created by Niels on 2017/11/13.
 */
public class UtxoDepositHandler extends NetworkNulsEventHandler<UtxoDepositEvent> {
    @Override
    public void onEvent(UtxoDepositEvent event) {
        //todo
    }
}

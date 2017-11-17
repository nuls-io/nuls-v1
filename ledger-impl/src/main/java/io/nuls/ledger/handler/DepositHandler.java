package io.nuls.ledger.handler;

import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.ledger.event.DepositEvent;
import io.nuls.ledger.event.TransferEvent;

/**
 * Created by Niels on 2017/11/13.
 */
public class DepositHandler extends NetworkNulsEventHandler<DepositEvent> {
    @Override
    public void onEvent(DepositEvent event) {
        //todo
    }
}

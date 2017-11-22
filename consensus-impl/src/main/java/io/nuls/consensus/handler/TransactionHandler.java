package io.nuls.consensus.handler;

import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.ledger.entity.AbstractCoinTransaction;
import io.nuls.ledger.event.AbstractCoinTransactionEvent;

/**
 * Created by facjas on 2017/11/16.
 */
public class TransactionHandler extends AbstractNetworkNulsEventHandler<AbstractCoinTransactionEvent<AbstractCoinTransaction>> {

    @Override
    public void onEvent(AbstractCoinTransactionEvent<AbstractCoinTransaction> event) throws NulsException{
    }
}

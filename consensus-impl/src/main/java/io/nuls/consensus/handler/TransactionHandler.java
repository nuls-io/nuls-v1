package io.nuls.consensus.handler;

import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.event.AbstractCoinTransactionEvent;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class TransactionHandler extends AbstractNetworkNulsEventHandler<AbstractCoinTransactionEvent<AbstractCoinTransaction>> {

    @Override
    public void onEvent(AbstractCoinTransactionEvent<AbstractCoinTransaction> event,String formId) throws NulsException{
    }
}

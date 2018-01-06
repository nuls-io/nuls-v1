package io.nuls.ledger.event;

import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.tx.SmallChangeTransaction;

/**
 *
 * @author Niels
 * 2017-10-10
 */
public class SmallChangeEvent <T extends SmallChangeTransaction> extends AbstractCoinTransactionEvent<T> {
    public SmallChangeEvent( ) {
        super(LedgerConstant.EVENT_TYPE_SMALL_CHANGE);
    }

    @Override
    public Object copy() {
        return null;
    }
}



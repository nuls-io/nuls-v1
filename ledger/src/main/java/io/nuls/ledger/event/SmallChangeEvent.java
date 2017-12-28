package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.SmallChangeTransaction;

public class SmallChangeEvent <T extends SmallChangeTransaction> extends AbstractCoinTransactionEvent<T> {

    public SmallChangeEvent(short eventType) {
        super(eventType);
    }

    @Override
    public Object copy() {
        return null;
    }
}



package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.UtxoSmallChangeTransaction;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoSmallChangeEvent<T extends UtxoSmallChangeTransaction> extends AbstractCoinTransactionEvent<T> {

    public UtxoSmallChangeEvent(short eventType) {
        super((short) 3);
    }

    @Override
    public Object copy() {
        // todo auto-generated method stub(niels)
        return null;
    }
}

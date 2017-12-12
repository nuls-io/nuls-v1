package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.TransferTransaction;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public class TransferEvent<T extends TransferTransaction> extends AbstractCoinTransactionEvent<T> {
    public TransferEvent() {
        super((short) 2);
    }

    @Override
    public Object copy() {
        // todo auto-generated method stub(niels)
        return null;
    }
}

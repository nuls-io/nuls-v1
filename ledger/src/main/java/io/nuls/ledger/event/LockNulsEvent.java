package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.LockNulsTransaction;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public class LockNulsEvent<T extends LockNulsTransaction> extends AbstractCoinTransactionEvent<T> {

    public LockNulsEvent() {
        super((short) 1);
    }

    @Override
    public Object copy() {
        // todo auto-generated method stub(niels)
        return null;
    }
}

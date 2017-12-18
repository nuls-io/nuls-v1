package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.UnlockNulsTransaction;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public class UnlockCoinEvent<T extends UnlockNulsTransaction> extends AbstractCoinTransactionEvent<T> {

    public UnlockCoinEvent() {
        super((short) 5);
    }

    @Override
    public Object copy() {
        // todo auto-generated method stub(niels)
        return null;
    }
}

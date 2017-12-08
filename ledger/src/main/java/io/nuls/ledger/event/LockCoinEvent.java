package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.LockCoinTransaction;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public class LockCoinEvent<T extends LockCoinTransaction> extends AbstractCoinTransactionEvent<T> {

    public LockCoinEvent() {
        super((short) 1);
    }

}

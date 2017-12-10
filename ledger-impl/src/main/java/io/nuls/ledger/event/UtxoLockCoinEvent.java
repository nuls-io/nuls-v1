package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.UtxoLockTransaction;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoLockCoinEvent<T extends UtxoLockTransaction> extends LockCoinEvent<T> {

    public UtxoLockCoinEvent() {
        super();
    }
}

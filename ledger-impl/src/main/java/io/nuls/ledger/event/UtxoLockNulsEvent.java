package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.UtxoLockTransaction;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoLockNulsEvent<T extends UtxoLockTransaction> extends LockNulsEvent<T> {

    public UtxoLockNulsEvent() {
        super();
    }
}

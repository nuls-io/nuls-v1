package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.UtxoDepositTransaction;

/**
 * Created by Niels on 2017/11/17.
 */
public class UtxoDepositCoinEvent<T extends UtxoDepositTransaction> extends LockCoinEvent<T> {

    public UtxoDepositCoinEvent() {
        super();
    }
}

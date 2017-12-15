package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.UtxoDepositTransaction;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class UtxoDepositNulsEvent<T extends UtxoDepositTransaction> extends LockNulsEvent<T> {

    public UtxoDepositNulsEvent() {
        super();
    }
}

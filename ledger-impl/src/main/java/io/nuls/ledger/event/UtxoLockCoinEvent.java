package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.utxoTransaction.UtxoLockTransaction;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoLockCoinEvent<T extends UtxoLockTransaction> extends LockCoinEvent<T> {

    public UtxoLockCoinEvent(NulsEventHeader header) {
        super(header);
    }
}

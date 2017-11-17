package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.utxoTransaction.UtxoLockTransaction;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoLockEvent<T extends UtxoLockTransaction> extends BaseUtxoCoinEvent<T> {

    public UtxoLockEvent(NulsEventHeader header) {
        super(header);
    }
}

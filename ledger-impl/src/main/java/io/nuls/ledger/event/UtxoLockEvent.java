package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoLockEvent<UtxoLockTransaction> extends BaseUtxoLedgerEvent {

    public UtxoLockEvent(NulsEventHeader header) {
        super(header);
    }
}

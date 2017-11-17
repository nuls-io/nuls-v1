package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.ledger.entity.utxoTransaction.UtxoLockTransaction;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoLockEvent<UtxoLockTransaction> extends UtxoBaseUtxoLedgerEvent {

    public UtxoLockEvent(NulsEventHeader header) {
        super(header);
    }
}

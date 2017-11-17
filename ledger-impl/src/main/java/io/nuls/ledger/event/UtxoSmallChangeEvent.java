package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.ledger.entity.utxoTransaction.UtxoSmallChangeTransaction;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoSmallChangeEvent<UtxoSmallChangeTransaction> extends UtxoBaseUtxoLedgerEvent {

    public UtxoSmallChangeEvent(NulsEventHeader header) {
        super(header);
    }
}

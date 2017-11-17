package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.utxoTransaction.UtxoCoinTransaction;

/**
 * Created by facjas on 2017/11/17.
 */
public class UtxoBaseUtxoLedgerEvent<UtxoCoinTransaction> extends BaseLedgerEvent {

    public UtxoBaseUtxoLedgerEvent(NulsEventHeader header) {
        super(header);
    }
}

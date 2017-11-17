package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;

/**
 * Created by Niels on 2017/11/8.
 *
 */
public class UtxoTransferEvent<UtxoTransferTransaction> extends BaseUtxoLedgerEvent {

    public UtxoTransferEvent(NulsEventHeader header) {
        super(header);
    }
}

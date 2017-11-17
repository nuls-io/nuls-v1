package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoSmallChangeEvent<UtxoSmallChangeTransaction> extends BaseUtxoLedgerEvent {

    public UtxoSmallChangeEvent(NulsEventHeader header) {
        super(header);
    }
}

package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;

/**
 * Created by facjas on 2017/11/17.
 */
public class BaseUtxoLedgerEvent<UtxoCoinTransaction> extends BaseLedgerEvent {

    public BaseUtxoLedgerEvent(NulsEventHeader header) {
        super(header);
    }
}

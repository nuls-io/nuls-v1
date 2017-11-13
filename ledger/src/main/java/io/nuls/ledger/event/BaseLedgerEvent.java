package io.nuls.ledger.event;

import io.nuls.core.event.NulsEvent;
import io.nuls.core.event.NulsEventHeader;

/**
 * Created by Niels on 2017/11/13.
 * nuls.io
 */
public abstract class BaseLedgerEvent extends NulsEvent {
    public BaseLedgerEvent(NulsEventHeader header) {
        super(header);
    }
}

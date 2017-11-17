package io.nuls.ledger.event;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.event.NulsEvent;
import io.nuls.core.event.NulsEventHeader;

/**
 * Created by Niels on 2017/11/16.
 */
public abstract class BaseLedgerEvent<T extends NulsData> extends NulsEvent<T>{
    public BaseLedgerEvent(NulsEventHeader header) {
        super(header);
    }
}

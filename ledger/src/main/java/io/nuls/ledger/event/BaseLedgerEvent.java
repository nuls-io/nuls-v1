package io.nuls.ledger.event;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.event.NulsEvent;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * Created by Niels on 2017/11/16.
 */
public class BaseLedgerEvent<T extends NulsData> extends NulsEvent<T>{
    public BaseLedgerEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected T parseEventBody(NulsByteBuffer byteBuffer) {
        return null;
    }
}

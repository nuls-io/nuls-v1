package io.nuls.ledger.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 *
 * @author Niels
 * @date 2017/11/16
 */
public class BaseLedgerEvent<T extends BaseNulsData> extends BaseNulsEvent<T>{
    public BaseLedgerEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected T parseEventBody(NulsByteBuffer byteBuffer) {
        return null;
    }
}

package io.nuls.ledger.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/11/16
 */
public abstract class BaseLedgerEvent<T extends BaseNulsData> extends BaseNulsEvent<T> {


    public BaseLedgerEvent(short eventType) {
        super(NulsConstant.MODULE_ID_LEDGER, eventType);
    }

    @Override
    protected T parseEventBody(NulsByteBuffer byteBuffer) {
        return null;
    }


}

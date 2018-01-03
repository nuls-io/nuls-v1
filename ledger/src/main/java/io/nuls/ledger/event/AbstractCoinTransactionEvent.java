package io.nuls.ledger.event;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

/**
 *
 * @author Niels
 * @date 2017/11/8
 *
 */
public abstract class AbstractCoinTransactionEvent<T extends AbstractCoinTransaction> extends BaseNetworkEvent<T> {

    public AbstractCoinTransactionEvent(short eventType) {
        super(NulsConstant.MODULE_ID_LEDGER, eventType);
    }

    @Override
    protected T parseEventBody(NulsByteBuffer byteBuffer) {
        return null;
    }
}

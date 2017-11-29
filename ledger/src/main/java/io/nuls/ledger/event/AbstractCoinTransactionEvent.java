package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.AbstractCoinTransaction;

/**
 *
 * @author Niels
 * @date 2017/11/8
 *
 */
public abstract class AbstractCoinTransactionEvent<T extends AbstractCoinTransaction> extends BaseLedgerEvent<T> {

    public AbstractCoinTransactionEvent(short eventType) {
        super(eventType);
    }

}

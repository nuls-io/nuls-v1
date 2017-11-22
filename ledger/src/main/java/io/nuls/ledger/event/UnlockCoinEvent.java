package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.UnlockCoinTransaction;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public class UnlockCoinEvent<T extends UnlockCoinTransaction> extends AbstractCoinTransactionEvent<T> {

    public UnlockCoinEvent(NulsEventHeader header) {
        super(header);
    }
}

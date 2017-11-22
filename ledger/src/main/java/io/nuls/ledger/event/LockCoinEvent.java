package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.LockCoinTransaction;

/**
 * Created by Niels on 2017/11/20.
 */
public class LockCoinEvent<T extends LockCoinTransaction> extends AbstractCoinTransactionEvent<T> {

    public LockCoinEvent(NulsEventHeader header) {
        super(header);
    }

}

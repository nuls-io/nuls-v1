package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.UnlockCoinTransaction;

/**
 * Created by Niels on 2017/11/20.
 */
public class UnlockCoinEvent<T extends UnlockCoinTransaction> extends CoinTransactionEvent<T> {

    public UnlockCoinEvent(NulsEventHeader header) {
        super(header);
    }
}

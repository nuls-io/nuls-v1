package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.CoinTransaction;

/**
 * Created by Niels on 2017/11/8.
 *
 */
public abstract class CoinTransactionEvent<T extends CoinTransaction> extends BaseLedgerEvent<T> {

    public CoinTransactionEvent(NulsEventHeader header) {
        super(header);
    }

}

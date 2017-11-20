package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.TransferTransaction;

/**
 * Created by Niels on 2017/11/20.
 */
public class TransferEvent<T extends TransferTransaction> extends CoinTransactionEvent<T> {
    public TransferEvent(NulsEventHeader header) {
        super(header);
    }
}

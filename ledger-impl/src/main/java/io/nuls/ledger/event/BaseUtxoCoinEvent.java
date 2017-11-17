package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.utxoTransaction.BaseUtxoCoinTransaction;

/**
 * Created by facjas on 2017/11/17.
 */
public class BaseUtxoCoinEvent<T extends BaseUtxoCoinTransaction> extends CoinTransactionEvent<T> {

    public BaseUtxoCoinEvent(NulsEventHeader header) {
        super(header);
    }
}

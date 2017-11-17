package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.utxoTransaction.UtxoSmallChangeTransaction;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoSmallChangeEvent<T extends UtxoSmallChangeTransaction> extends BaseUtxoCoinEvent<T> {

    public UtxoSmallChangeEvent(NulsEventHeader header) {
        super(header);
    }
}

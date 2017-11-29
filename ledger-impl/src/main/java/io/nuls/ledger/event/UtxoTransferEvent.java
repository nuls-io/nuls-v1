package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.utxoTransaction.UtxoTransferTransaction;

/**
 * Created by Niels on 2017/11/8.
 */
public class UtxoTransferEvent<T extends UtxoTransferTransaction> extends TransferEvent<T> {

    public UtxoTransferEvent() {
        super();
    }
}

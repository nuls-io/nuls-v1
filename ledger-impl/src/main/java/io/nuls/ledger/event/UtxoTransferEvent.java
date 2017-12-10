package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.UtxoTransferTransaction;

/**
 * Created by Niels on 2017/11/8.
 */
public class UtxoTransferEvent<T extends UtxoTransferTransaction> extends TransferEvent<T> {

    public UtxoTransferEvent() {
        super();
    }
}

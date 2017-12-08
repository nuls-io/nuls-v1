package io.nuls.ledger.event;

import io.nuls.ledger.entity.tx.TransferTransaction;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public class TransferEvent<T extends TransferTransaction> extends AbstractCoinTransactionEvent<T> {
    public TransferEvent() {
        super((short) 2);
    }
}

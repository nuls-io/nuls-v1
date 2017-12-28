package io.nuls.ledger.event;

import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.tx.TransferTransaction;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public class TransferCoinEvent<T extends TransferTransaction> extends AbstractCoinTransactionEvent<T> {
    public TransferCoinEvent() {
        super(LedgerConstant.EVENT_TYPE_TRANSFER);
    }

    @Override
    public Object copy() {
        // todo auto-generated method stub(niels)
        return null;
    }
}

package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.ledger.entity.TransferTransaction;

/**
 * Created by Niels on 2017/11/17.
 */
public class TransferEvent extends CoinTransactionEvent<TransferTransaction> {

    public TransferEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected TransferTransaction parseEventBody(ByteBuffer byteBuffer) {
        TransferTransaction tx = new TransferTransaction();
        tx.parse(byteBuffer);
        return tx;
    }
}

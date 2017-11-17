package io.nuls.ledger.event;

import io.nuls.core.event.NulsEvent;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.ledger.entity.CoinTransaction;
import io.nuls.ledger.entity.SmallChangeTransaction;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class SmallChangeEvent extends CoinTransactionEvent<SmallChangeTransaction> {
    public SmallChangeEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected SmallChangeTransaction parseEventBody(ByteBuffer byteBuffer) {
        SmallChangeTransaction tx = new SmallChangeTransaction();
        tx.parse(byteBuffer);
        return tx;
    }
}

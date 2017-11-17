package io.nuls.ledger.event;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.event.NulsEvent;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.ledger.entity.LockTransaction;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/13.
 */
public class LockEvent extends CoinTransactionEvent<LockTransaction> {
    public LockEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected LockTransaction parseEventBody(ByteBuffer byteBuffer) {
        LockTransaction lockTx = new LockTransaction();
        lockTx.parse(byteBuffer);
        return lockTx;
    }

}

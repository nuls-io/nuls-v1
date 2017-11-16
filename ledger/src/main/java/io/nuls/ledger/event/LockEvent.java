package io.nuls.ledger.event;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.ledger.entity.LockTransaction;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class LockEvent extends BaseLedgerEvent {
    public LockEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected NulsData parseEventBody(ByteBuffer byteBuffer) {
        return null;
    }


    private LockTransaction tx;


    public LockTransaction getTx() {
        return tx;
    }

    public void setTx(LockTransaction tx) {
        this.tx = tx;
    }
}

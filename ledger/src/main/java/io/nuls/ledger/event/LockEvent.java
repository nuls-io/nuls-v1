package io.nuls.ledger.event;

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


    private LockTransaction tx;


    @Override
    public int size() {
        int size = super.size();
        if(null!=tx){
            size += tx.size();
        }
        return size;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        super.serializeToStream(stream);
        if(null!=tx){
            tx.serializeToStream(stream);
        }
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        super.parse(byteBuffer);
        tx = new LockTransaction();
        tx.parse(byteBuffer);
    }

    @Override
    public void verify() throws NulsException {
        //todo
    }
}

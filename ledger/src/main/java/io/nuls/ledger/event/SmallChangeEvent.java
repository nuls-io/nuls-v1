package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/13.
 * nuls.io
 */
public class SmallChangeEvent extends BaseLedgerEvent{
    public SmallChangeEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    public int size() {
        //todo
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        //todo
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        //todo
    }

    @Override
    public void verify() throws NulsException {
        //todo
    }
}

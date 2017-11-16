package io.nuls.core.event;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/7.
 *
 */
public class NulsEvent extends NulsData {

    public NulsEvent(NulsEventHeader header) {
        this.header = header;
    }

    private NulsEventHeader header;

    public NulsEventHeader getHeader() {
        return header;
    }

    public void setHeader(NulsEventHeader header) {
        this.header = header;
    }

    @Override
    public int size() {
        return header.size();
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        this.header.serializeToStream(stream);
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        this.header = new NulsEventHeader();
        this.header.parse(byteBuffer);
    }

}

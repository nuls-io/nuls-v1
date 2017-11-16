package io.nuls.core.event;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/7.
 */
public abstract class NulsEvent extends NulsData {
    private NulsEventHeader header;

    private NulsData eventBody;

    public NulsEvent(NulsEventHeader header) {
        this.header = header;
    }


    @Override
    public final int size() {
        return header.size() + eventBody.size();
    }

    @Override
    public final void serializeToStream(OutputStream stream) throws IOException {
        this.header.serializeToStream(stream);
        this.eventBody.serializeToStream(stream);
    }

    @Override
    public final void parse(ByteBuffer byteBuffer) {
        this.header = new NulsEventHeader();
        this.header.parse(byteBuffer);
        this.eventBody = parseEventBody(byteBuffer);
    }

    protected abstract NulsData parseEventBody(ByteBuffer byteBuffer);

    public NulsData getEventBody() {
        return eventBody;
    }

    public void setEventBody(NulsData eventBody) {
        this.eventBody = eventBody;
    }
    public NulsEventHeader getHeader() {
        return header;
    }

    public void setHeader(NulsEventHeader header) {
        this.header = header;
    }
}

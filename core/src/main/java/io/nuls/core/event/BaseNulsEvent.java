package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/7.
 */
public abstract class BaseNulsEvent<T extends BaseNulsData> extends BaseNulsData {
    private NulsEventHeader header;

    private T eventBody;

    public BaseNulsEvent(NulsEventHeader header) {
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
    public final void parse(NulsByteBuffer byteBuffer) {
        this.header = new NulsEventHeader();
        this.header.parse(byteBuffer);
        this.eventBody = parseEventBody(byteBuffer);
    }

    protected abstract T parseEventBody(NulsByteBuffer byteBuffer);

    public T getEventBody() {
        return eventBody;
    }

    public void setEventBody(T eventBody) {
        this.eventBody = eventBody;
    }
    public NulsEventHeader getHeader() {
        return header;
    }

    public void setHeader(NulsEventHeader header) {
        this.header = header;
    }
}

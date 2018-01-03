package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseEvent<T extends BaseNulsData> extends BaseNulsData implements NulsCloneable{
    private EventHeader header;
    private T eventBody;

    public BaseEvent(short moduleId, short eventType, byte[] extend) {
        this.header = new EventHeader(moduleId, eventType, extend);
    }

    @Override
    public int size() {
        if (eventBody != null) {
            return header.size() + eventBody.size();
        } else {
            return header.size();
        }
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        this.header.serializeToStream(stream);
        stream.writeNulsData(this.eventBody);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.header = new EventHeader();
        this.header.parse(byteBuffer);
        this.eventBody = parseEventBody(byteBuffer);
    }

    protected abstract T parseEventBody(NulsByteBuffer byteBuffer) throws NulsException;

    public T getEventBody() {
        return eventBody;
    }

    public void setEventBody(T eventBody) {
        this.eventBody = eventBody;
    }

    public EventHeader getHeader() {
        return header;
    }

    public void setHeader(EventHeader header) {
        this.header = header;
    }

}

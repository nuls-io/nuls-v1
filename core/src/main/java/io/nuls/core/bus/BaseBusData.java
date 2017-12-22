package io.nuls.core.bus;

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
public abstract class BaseBusData<T extends BaseNulsData> extends BaseNulsData implements NulsCloneable{
    private BusDataHeader header;
    private T eventBody;

    public BaseBusData(short moduleId, short eventType, byte[] extend) {
        this.header = new BusDataHeader(moduleId, eventType, extend);
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
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        this.header.serializeToStream(stream);
        if (eventBody != null) {
            this.eventBody.serializeToStream(stream);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.header = new BusDataHeader();
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

    public BusDataHeader getHeader() {
        return header;
    }

    public void setHeader(BusDataHeader header) {
        this.header = header;
    }

}

package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/7.
 */
public class NulsEventHeader extends BaseNulsData {
    private short moduleId;
    private short eventType;

    public NulsEventHeader() {
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(Utils.shortToBytes(moduleId));
        stream.write(Utils.shortToBytes(eventType));
    }

    public NulsEventHeader(short moduleId, short eventType) {
        this.moduleId = moduleId;
        this.eventType = eventType;
    }

    public short getEventType() {
        return eventType;
    }

    public short getModuleId() {
        return moduleId;
    }

    @Override
    public void parse(NulsByteBuffer buffer) {
        this.moduleId = buffer.readShort();
        this.eventType = buffer.readShort();
    }
}

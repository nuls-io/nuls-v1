package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Niels
 * @date 2017/11/7
 */
public class NulsEventHeader extends BaseNulsData {
    private static final int EVENT_HEADER_LENGHT = 10;
    private static final int EVENT_HEADER_EXTEND_LENGHT = 6;
    private short moduleId;
    private short eventType;
    private byte[] extend;

    public NulsEventHeader(short moduleId, short eventType,byte[] extend) {
        this.moduleId = moduleId;
        this.eventType = eventType;
        this.extend = extend;
        checkExtend();

    }

    protected void checkExtend(){
        if(extend == null || extend.length ==0){
            extend = new byte[EVENT_HEADER_EXTEND_LENGHT];
        }
    }

    public NulsEventHeader(short moduleId, short eventType) {
        this(moduleId,eventType,null);
    }

    public NulsEventHeader(short eventType){
        this((short)(0),eventType);
    }

    public NulsEventHeader() {
        this((short) 0,(short) 0);
    }

    @Override
    public int size() {
        return EVENT_HEADER_LENGHT;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        byte[] header = serialize();
        stream.write(header);
    }

    @Override
    public byte[] serialize() {
        byte[] header = new byte[EVENT_HEADER_LENGHT];
        Utils.int16ToByteArrayLE(moduleId, header, 0);
        Utils.int16ToByteArrayLE(eventType, header, 2);
        checkExtend();
        System.arraycopy(extend,0,header,4,EVENT_HEADER_EXTEND_LENGHT);
        return header;
    }

    public short getEventType() {
        return eventType;
    }

    public void setEventType(short eventType){
        this.eventType = eventType;
    }

    public short getModuleId() {
        return moduleId;
    }

    public void setModuleId(short moduleId){
        this.moduleId = moduleId;
    }

    @Override
    public void parse(NulsByteBuffer buffer) {
        this.moduleId = buffer.readInt16LE();
        this.eventType = buffer.readInt16LE();
        this.extend = buffer.readBytes(EVENT_HEADER_EXTEND_LENGHT);
    }
}

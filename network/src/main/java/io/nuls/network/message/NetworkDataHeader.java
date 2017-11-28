package io.nuls.network.message;

import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vivi
 * @date 2017/11/28.
 */
public class NetworkDataHeader {

    public static final int NETWORK_HEADER_SIZE = 10;

    public static final int EXTEND_LENGTH = 6;

    private short moduleId;

    private short type;

    private byte[] extend;

    public NetworkDataHeader() {

    }

    public NetworkDataHeader(NulsByteBuffer buffer) {
        this.parse(buffer);
    }

    public NetworkDataHeader(short moduleId, short type) {
        this.moduleId = moduleId;
        this.type = type;
        this.extend = new byte[EXTEND_LENGTH];
    }

    public NetworkDataHeader(short moduleId, short type, byte[] extend) {
        this.moduleId = moduleId;
        this.type = type;
        this.extend = extend;
    }

    public byte[] serialize() {
        byte[] header = new byte[NETWORK_HEADER_SIZE];
        Utils.uint16ToByteArrayLE(moduleId, header, 0);
        Utils.uint16ToByteArrayLE(type, header, 2);

        System.arraycopy(extend, 0, header, 4, 6);
        return header;
    }

    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(serialize());
    }

    public void parse(NulsByteBuffer byteBuffer) {
        this.moduleId = byteBuffer.readShort();
        this.type = byteBuffer.readShort();
        this.extend = byteBuffer.readBytes(EXTEND_LENGTH);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("networkHeader:{");
        buffer.append("moduleId:" + moduleId + ", ");
        buffer.append("type:" + type + ", ");
        buffer.append("extend:[");
        if (extend != null && extend.length > 0) {
            for (byte b : extend) {
                buffer.append(b + " ");
            }
        }
        buffer.append("]}");
        return buffer.toString();
    }

    public short getModuleId() {
        return moduleId;
    }

    public void setModuleId(short moduleId) {
        this.moduleId = moduleId;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

}

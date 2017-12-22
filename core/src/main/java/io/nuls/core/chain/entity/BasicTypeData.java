package io.nuls.core.chain.entity;

import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/7
 */
public class BasicTypeData<T> extends BaseNulsData {

    private int type;

    private T val;

    public BasicTypeData(T data) {
        this.val = data;
        this.type = getType();
    }

    public BasicTypeData(NulsByteBuffer buffer) throws NulsException {
        this.parse(buffer);
    }

    @Override
    public int size() {
        int size = 1;
        size += Utils.sizeOfSerialize(val);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(type);
        switch (type) {
            case 1:
                stream.writeString((String) val);
                break;
            case 2:
                stream.writeVarInt((Long) val);
                break;
            case 3:
                stream.writeVarInt((Integer) val);
                break;
            case 4:
                stream.writeDouble((Double) val);
                break;
            case 5:
                stream.writeShort((Short) val);
                break;
            case 6:
                stream.writeBoolean((Boolean) val);
                break;
            case 7:
                stream.writeBytesWithLength((byte[]) val);
                break;
            default:
                break;
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.type = byteBuffer.readByte();
        switch (type) {
            case 1:
                this.val = (T) byteBuffer.readString();
                break;
            case 2:
                this.val = (T) ((Object) byteBuffer.readVarInt());
                break;
            case 3:
                this.val = (T) ((Object) byteBuffer.readVarInt());
                break;
            case 4:
                this.val = (T) ((Object) byteBuffer.readDouble());
                break;
            case 5:
                this.val = (T) ((Object) byteBuffer.readVarInt());
                break;
            case 6:
                this.val = (T) ((Object) byteBuffer.readBoolean());
                break;
            case 7:
                this.val = (T) byteBuffer.readByLengthByte();
                break;
            default:
                break;
        }
    }

    public T getVal() {
        return val;
    }

    public int getType() {
        if (val instanceof String) {
            return 1;
        } else if (val instanceof Long) {
            return 2;
        } else if (val instanceof Integer) {
            return 3;
        } else if (val instanceof Double) {
            return 4;
        } else if (val instanceof Short) {
            return 5;
        } else if (val instanceof Boolean) {
            return 6;
        } else if (val instanceof byte[]) {
            return 7;
        } else {
            return 8;
        }
    }
}

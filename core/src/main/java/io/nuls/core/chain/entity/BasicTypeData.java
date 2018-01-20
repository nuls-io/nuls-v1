/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

    public BasicTypeData() {
    }

    @Override
    public int size() {
        int size = 1;
        size += Utils.sizeOfSerialize(val);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
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
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.type = byteBuffer.readByte();
        switch (type) {
            case 1:
                this.val = (T) byteBuffer.readString();
                break;
            case 2:
                this.val = (T) ((Object) byteBuffer.readVarInt());
                break;
            case 3:
                this.val = (T) ((Object) ((Long) byteBuffer.readVarInt()).intValue());
                break;
            case 4:
                this.val = (T) ((Object) byteBuffer.readDouble());
                break;
            case 5:
                this.val = (T) ((Object) ((Long) byteBuffer.readVarInt()).shortValue());
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
            return 0;
        }
    }
}

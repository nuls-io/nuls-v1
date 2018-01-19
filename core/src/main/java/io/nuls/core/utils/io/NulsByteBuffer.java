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
package io.nuls.core.utils.io;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.log.Log;

import java.io.UnsupportedEncodingException;

/**
 * @author Niels
 * @date 2017/11/2
 */
public class NulsByteBuffer {

    private final byte[] payload;

    private int cursor;

    public NulsByteBuffer(byte[] bytes) {
        this(bytes, 0);
    }

    public NulsByteBuffer(byte[] bytes, int cursor) {
        if (null == bytes || bytes.length == 0 || cursor < 0) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "create byte buffer faild!");
        }
        this.payload = bytes;
        this.cursor = cursor;
    }

    public long readUint32LE() throws NulsException {
        try {
            long u = Utils.readUint32LE(payload, cursor);
            cursor += 4;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NulsException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public short readInt16LE() throws NulsException {
        try {
            short s = Utils.readInt16LE(payload, cursor);
            cursor += 2;
            return s;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NulsException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public int readInt32LE() throws NulsException {
        try {
            int u = Utils.readInt32LE(payload, cursor);
            cursor += 4;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NulsException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public long readUint32() throws NulsException {
        return (long) readInt32LE();
    }

    public long readInt64() throws NulsException {
        try {
            long u = Utils.readInt64LE(payload, cursor);
            cursor += 8;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NulsException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }


    public long readVarInt() throws NulsException {
        return readVarInt(0);
    }

    public long readVarInt(int offset) throws NulsException {
        try {
            VarInt varint = new VarInt(payload, cursor + offset);
            cursor += offset + varint.getOriginalSizeInBytes();
            return varint.value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NulsException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public byte readByte() throws NulsException {
        try {
            byte b = payload[cursor];
            cursor += 1;
            return b;
        } catch (IndexOutOfBoundsException e) {
            throw new NulsException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public byte[] readBytes(int length) throws NulsException {
        try {
            byte[] b = new byte[length];
            System.arraycopy(payload, cursor, b, 0, length);
            cursor += length;
            return b;
        } catch (IndexOutOfBoundsException e) {
            throw new NulsException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public byte[] readByLengthByte() throws NulsException {
        long length = this.readVarInt();
        if (length == 0) {
            return null;
        }
        return readBytes((int) length);
    }

    public boolean readBoolean() throws NulsException {
        byte b = readByte();
        return 1 == b;
    }

    public NulsDigestData readHash() throws NulsException {
        return this.readNulsData(new NulsDigestData());
    }

    public void resetCursor() {
        this.cursor = 0;
    }

    public short readShort() throws NulsException {
        return Utils.bytes2Short(readBytes(2));
    }

    public String readString() throws NulsException {
        try {
            return new String(this.readByLengthByte(), NulsContext.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            throw new NulsException(e);
        }

    }

    public double readDouble() throws NulsException {
        return Utils.bytes2Double(this.readByLengthByte());
    }

    public boolean isFinished() {
        return this.payload.length == cursor;
    }

    public byte[] getPayloadByCursor() {
        byte[] bytes = new byte[payload.length - cursor];
        System.arraycopy(this.payload, cursor, bytes, 0, bytes.length);
        return bytes;
    }

    public byte[] getPayload() {
        return payload;
    }

    public <T extends BaseNulsData> T readNulsData(T nulsData) throws NulsException {
        if (payload == null || payload.length == 0 || NulsConstant.PLACE_HOLDER == payload[0]) {
            return null;
        }
        int length = payload.length - cursor;
        byte[] bytes = new byte[length];
        System.arraycopy(payload, cursor, bytes, 0, length);
        nulsData.parse(bytes);
        cursor += nulsData.size();
        return nulsData;
    }

    public NulsSignData readSign() throws NulsException {
        return this.readNulsData(new NulsSignData());
    }

    public Transaction readTransaction() throws NulsException {
        try {
            return TransactionManager.getInstance(this);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(ErrorCode.DATA_PARSE_ERROR);
        }
    }
}

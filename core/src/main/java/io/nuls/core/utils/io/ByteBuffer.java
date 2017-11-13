package io.nuls.core.utils.io;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.exception.VerificationException;
import io.nuls.core.utils.crypto.Utils;

/**
 * Created by Niels on 2017/11/2.
 * nuls.io
 */
public class ByteBuffer {

    private final byte[] payload;

    private int cursor;

    public ByteBuffer(byte[] bytes) {
        this(bytes, 0);
    }

    public ByteBuffer(byte[] bytes, int cursor) {
        if (null == bytes || bytes.length == 0 || cursor < 0) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "create byte buffer faild!");
        }
        this.payload = bytes;
        this.cursor = cursor;
    }

    public long readUint32() throws VerificationException {
        try {
            long u = Utils.readUint32(payload, cursor);
            cursor += 4;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new VerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }


    public long readInt64() throws VerificationException {
        try {
            long u = Utils.readInt64(payload, cursor);
            cursor += 8;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new VerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }
    public int readInt32() throws VerificationException {
        try {
            int u = Utils.readInt32(payload, cursor);
            cursor += 4;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new VerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }
    public long readVarInt() throws VerificationException {
        return readVarInt(0);
    }

    public long readVarInt(int offset) throws VerificationException {
        try {
            VarInt varint = new VarInt(payload, cursor + offset);
            cursor += offset + varint.getOriginalSizeInBytes();
            return varint.value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new VerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public byte[] readBytes(int length) throws VerificationException {
        if (length > Block.MAX_SIZE) {
            throw new VerificationException(ErrorCode.DATA_OVER_SIZE_ERROR);
        }
        try {
            byte[] b = new byte[length];
            System.arraycopy(payload, cursor, b, 0, length);
            cursor += length;
            return b;
        } catch (IndexOutOfBoundsException e) {
            throw new VerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public byte[] readByLengthByte() {
        int length = this.readBytes(1)[0] & 0xff;
        return readBytes(length);
    }

    public Sha256Hash readHash() {
        return Sha256Hash.wrapReversed(readBytes(32));
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public void resetCursor() {
        this.cursor = 0;
    }

    public short readShort() {
        return Utils.bytes2Short(readBytes(2));
    }
}

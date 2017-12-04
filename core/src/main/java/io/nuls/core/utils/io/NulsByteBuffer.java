package io.nuls.core.utils.io;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.utils.crypto.Utils;

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

    public long readUint32LE() throws NulsVerificationException {
        try {
            long u = Utils.readUint32LE(payload, cursor);
            cursor += 4;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NulsVerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public short readInt16LE() throws NulsVerificationException {
        try {
            short s = Utils.readInt16LE(payload, cursor);
            cursor += 2;
            return s;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NulsVerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public int readInt32LE() throws NulsVerificationException {
        try {
            int u = Utils.readInt32LE(payload, cursor);
            cursor += 4;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NulsVerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public long readUint32() throws NulsVerificationException {
        return (long) readInt32LE();
    }

    public long readInt64() throws NulsVerificationException {
        try {
            long u = Utils.readInt64LE(payload, cursor);
            cursor += 8;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NulsVerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }


    public long readVarInt() throws NulsVerificationException {
        return readVarInt(0);
    }

    public long readVarInt(int offset) throws NulsVerificationException {
        try {
            VarInt varint = new VarInt(payload, cursor + offset);
            cursor += offset + varint.getOriginalSizeInBytes();
            return varint.value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NulsVerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public byte readByte() throws NulsVerificationException {
        try {
            byte b = payload[cursor];
            cursor += 1;
            return b;
        } catch (IndexOutOfBoundsException e) {
            throw new NulsVerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public byte[] readBytes(int length) throws NulsVerificationException {
        if (length > Block.MAX_SIZE) {
            throw new NulsVerificationException(ErrorCode.DATA_OVER_SIZE_ERROR);
        }
        try {
            byte[] b = new byte[length];
            System.arraycopy(payload, cursor, b, 0, length);
            cursor += length;
            return b;
        } catch (IndexOutOfBoundsException e) {
            throw new NulsVerificationException(ErrorCode.DATA_PARSE_ERROR, e);
        }
    }

    public byte[] readByLengthByte() {
        long length = this.readVarInt();
        return readBytes((int) length);
    }

    public boolean readBoolean() {
        byte b = readByte();
        return 1 == b;
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

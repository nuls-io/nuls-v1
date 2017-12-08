package io.nuls.core.utils.io;

import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class NulsOutputStreamBuffer {

    private final OutputStream out;

    public NulsOutputStreamBuffer(OutputStream out) {
        this.out = out;
    }

    public void write(byte[] bytes) throws IOException {
        out.write(bytes);
    }

    public void write(int val) throws IOException {
        out.write(val);
    }

    public void writeVarInt(int val) throws IOException {
        out.write(new VarInt(val).encode());
    }

    public void writeVarInt(long val) throws IOException {
        out.write(new VarInt(val).encode());
    }

    public void writeBytesWithLength(String value) throws IOException {
        if (StringUtils.isBlank(value)) {
            out.write(new VarInt(0).encode());
            return;
        }
        this.writeBytesWithLength(value.getBytes(NulsContext.DEFAULT_ENCODING));
    }

    public void writeBytesWithLength(byte[] bytes) throws IOException {
        if (null == bytes || bytes.length == 0) {
            out.write(new VarInt(0).encode());
        } else {
            out.write(new VarInt(bytes.length).encode());
            out.write(bytes);
        }
    }

    public void writeBoolean(boolean val) throws IOException {
        out.write(val ? 1 : 0);
    }

    public void writeShort(short val) throws IOException {
        Utils.int16ToByteStreamLE(val, out);
    }

    public void writeInt32(int val) throws IOException {
        Utils.uint32ToByteStreamLE(val, out);
    }

    public void writeInt64(long val) throws IOException {
        Utils.int64ToByteStreamLE(val, out);
    }

    public void writeDouble(double val) throws IOException {
        this.writeBytesWithLength(Utils.double2Bytes(val));
    }
}

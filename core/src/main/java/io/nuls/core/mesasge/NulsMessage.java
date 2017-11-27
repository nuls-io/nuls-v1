package io.nuls.core.mesasge;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NulsMessage {

    public static final int MAX_SIZE = NulsMessageHeader.MESSAGE_HEADER_SIZE + Block.MAX_SIZE;

    protected NulsMessageHeader header;

    protected byte[] data;

    public NulsMessage() {
        this.header = new NulsMessageHeader();
        this.data = new byte[0];
    }

    public NulsMessage(NulsMessageHeader header, byte[] data) {
        if (header == null) {
            header = new NulsMessageHeader();
        }
        if (data == null) {
            data = new byte[0];
        }
        this.header = header;
        this.data = data;
        caculateXor();
        header.setLength(data.length);
    }

    public NulsMessage(byte[] data) {
        this(null, data);
    }

    public NulsMessage(NulsMessageHeader header) {
        this(header, null);
    }

    public NulsMessage(int magicNumber, short msgType) {
        this();
        this.header.setMagicNumber(magicNumber);
        this.header.setHeadType(msgType);
    }

    public NulsMessage(int magicNumber, short msgType, byte[] data) {
        this(data);
        this.header.setMagicNumber(magicNumber);
        this.header.setHeadType(msgType);
    }

    public NulsMessage(int magicNumber, short msgType, byte[] extend, byte[] data) {
        this(magicNumber, msgType, data);
        if (extend == null) {
            extend = new byte[9];
        }
        this.header.setExtend(extend);
    }

    public NulsMessage(int magicNumber, int length, short msgType, byte xor, byte[] data) {
        this.header = new NulsMessageHeader(magicNumber, msgType, length, xor);
        this.data = data;
    }

    public NulsMessage(int magicNumber, int length, short msgType, byte xor, byte[] extend, byte[] data) {
        this.header = new NulsMessageHeader(magicNumber, msgType, length, xor, extend);
        this.data = data;
    }

    public NulsMessageHeader getHeader() {
        return header;
    }

    public byte caculateXor() {
        if (header == null || data == null || data.length == 0) {
            return 0x00;
        }
        byte xor = 0x00;
        for (int i = 0; i < data.length; i++) {
            xor ^= data[i];
        }
        header.setXor(xor);
        return xor;
    }

    public byte[] serialize() throws IOException {
        byte[] value = new byte[NulsMessageHeader.MESSAGE_HEADER_SIZE + data.length];
        byte[] headerBytes = header.serialize();
        System.arraycopy(headerBytes, 0, value, 0, headerBytes.length);
        System.arraycopy(data, 0, value, headerBytes.length, data.length);
        return value;
    }

    public void setHeader(NulsMessageHeader header) {
        this.header = header;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    void verify() throws NulsVerificationException {
        if (this.header == null || this.data == null) {
            throw new NulsVerificationException(ErrorCode.NET_MESSAGE_ERROR);
        }

        if (header.getLength() != data.length) {
            throw new NulsVerificationException(ErrorCode.NET_MESSAGE_LENGTH_ERROR);
        }

        if (header.getXor() != caculateXor()) {
            throw new NulsVerificationException(ErrorCode.NET_MESSAGE_XOR_ERROR);
        }
    }

    public static void main(String[] args) throws IOException {
        NulsMessageHeader header = new NulsMessageHeader(12345678, (short) 1, 500, (byte) 3);
        NulsMessage msg = new NulsMessage(header, "Nuls test message".getBytes());


        byte[] messges = msg.serialize();






//        try {
//            System.out.println(Hex.encode(msg.serialize()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        msg.verify();
    }
}

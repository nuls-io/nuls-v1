package io.nuls.core.mesasge;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsVerificationException;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author vivi
 * @date 2017-11-10
 */
public class NulsMessage implements Serializable {

    public static final int MAX_SIZE = NulsMessageHeader.MESSAGE_HEADER_SIZE + Block.MAX_SIZE;

    protected NulsMessageHeader header;

    protected byte[] data;

    public NulsMessage() {
        this.header = new NulsMessageHeader();
        this.data = new byte[0];
    }

    public NulsMessage(NulsMessageHeader header) {
        this.header = header;
    }

    public NulsMessage(NulsMessageHeader header, byte[] data) {
        this.header = header;
        this.data = data;
        caculateXor();
        header.setLength(data.length);
    }


    public NulsMessage(int magicNumber, short msgType) {
        this.header = new NulsMessageHeader(magicNumber, msgType);
    }

    public NulsMessage(int magicNumber, short msgType, byte[] data) {
        this(magicNumber, msgType);
        this.data = data;
        caculateXor();
        header.setLength(data.length);
    }

    public NulsMessage(int magicNumber, short msgType, byte[] data, byte[] extend) {
        this(magicNumber, msgType, data);
        this.header.setExtend(extend);
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
        byte[] value = new byte[MAX_SIZE + data.length];
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
        caculateXor();
        header.setLength(data.length);
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

}

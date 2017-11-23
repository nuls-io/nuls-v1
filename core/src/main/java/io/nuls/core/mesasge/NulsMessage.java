package io.nuls.core.mesasge;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.crypto.VarInt;

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

    public NulsMessage(byte[] data) {
        this();
        this.data = data;
        byte xor = 0x00;
        for(int i=0;i<data.length;i++){
            xor ^= data[i];
        }
        this.header.setXor(xor);
    }

    public NulsMessage(NulsMessageHeader header) {
        this.header = header;
        this.data = new byte[0];
    }

    public NulsMessage(NulsMessageHeader header, byte[] data) {
        this.header = header;
        this.data = data;
        byte xor = 0x00;
        for(int i=0;i<data.length;i++){
            xor ^= data[i];
        }
        header.setXor(xor);
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
        this(magicNumber,msgType,data);
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
    }


}

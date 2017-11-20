package io.nuls.core.mesasge;

import io.nuls.core.chain.entity.Block;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NulsMessage {

    public static final int MAX_SIZE = NulsMessageHeader.MESSAGE_HEADER_SIZE + Block.MAX_SIZE;

    private NulsMessageHeader header;

    private byte[] data;

    public NulsMessage(NulsMessageHeader header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    public NulsMessage(int magicNumber, short msgType) {
        this.header = new NulsMessageHeader(magicNumber, msgType);
        this.data = data;
    }

    public NulsMessage(int magicNumber, short msgType, byte[] data) {
        this.header = new NulsMessageHeader(magicNumber, msgType);
        this.data = data;
    }

    public NulsMessage(int magicNumber, short msgType, byte[] extend, byte[] data) {
        this.header = new NulsMessageHeader(magicNumber, msgType, extend);
        this.data = data;
    }

    public NulsMessage(byte[] data) {
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

    public static void main(String[] args) {
        ByteBuffer buffer = java.nio.ByteBuffer.allocate(200);
        byte[] b1 = new byte[]{10, 11, 12, 13};
        byte[] b2 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] b3 = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, b3, 0, b1.length);
        System.arraycopy(b2, 0, b3, b1.length, b2.length);
        for(byte b : b3) {
            System.out.print(b + ",");
        }

    }
}

package io.nuls.core.mesasge;

import io.nuls.core.chain.entity.Block;

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

package io.nuls.core.mesasge;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.mesasge.validator.NulsMessageValidator;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NulsMessage extends BaseNulsData{

    public static final int MAX_SIZE = NulsMessageHeader.MESSAGE_HEADER_SIZE + Block.MAX_SIZE;

    protected NulsMessageHeader header;

    protected byte[] data;

    public NulsMessage() {
        this.header = new NulsMessageHeader();
        this.data = new byte[0];
        this.registerValidator(new NulsMessageValidator());
    }

    public NulsMessage(ByteBuffer buffer) {
        parse(new NulsByteBuffer(buffer.array()));
    }


    public NulsMessage(NulsMessageHeader header, byte[] data) {
        this.header = header;
        this.data = data;
        caculateXor();
        header.setLength(data.length);
    }

    public NulsMessage(byte[] data) {
        this.header = new NulsMessageHeader();
        this.data = data;
        caculateXor();
        header.setLength(data.length);
    }

    public NulsMessage(NulsMessageHeader header) {
        this.header = new NulsMessageHeader();
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
//
//    public NulsMessage(int magicNumber, int length, short msgType, byte xor, byte[] data) {
//        this.header = new NulsMessageHeader(magicNumber, msgType, length, xor);
//        this.data = data;
//    }
//
//    public NulsMessage(int magicNumber, int length, short msgType, byte xor, byte[] extend, byte[] data) {
//        this.header = new NulsMessageHeader(magicNumber, msgType, length, xor, extend);
//        this.data = data;
//    }

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

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(header.serialize());
        stream.write(data);
    }

    @Override
    protected int dataSize() {
        return 0;
    }

    @Override
    protected void parseObject(NulsByteBuffer byteBuffer) {

    }

    protected void parseObject(ByteBuffer byteBuffer) {
        byte[] headers = new byte[NulsMessageHeader.MESSAGE_HEADER_SIZE];
        byteBuffer.get(headers, 0, headers.length);
        NulsMessageHeader header = new NulsMessageHeader(new NulsByteBuffer(headers));
        byte[] data = new byte[byteBuffer.limit() - headers.length];
        byteBuffer.get(data, 0, data.length);
        this.header = header;
        this.data = data;
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

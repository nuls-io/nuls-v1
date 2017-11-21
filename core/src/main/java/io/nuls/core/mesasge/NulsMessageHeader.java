package io.nuls.core.mesasge;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

public class NulsMessageHeader extends NulsData {

    public static final int MESSAGE_HEADER_SIZE = 20;

    private int magicNumber;

    // the message length
    private int length;


    //0x01 : networkMessage  //0x02 : eventMessage;
    private short headType;
    public static final short NETWORK_MESSAGE = 1;
    public static final short EVENT_MESSAGE = 2;

    //the extend length
    public static final int EXTEND_LENGTH = 8;
    private byte[] extend;

    public NulsMessageHeader() {
        super();
    }

    public NulsMessageHeader(int magicNumber, short headType) {
        this.magicNumber = magicNumber;
        this.headType = headType;
    }

    public NulsMessageHeader(int magicNumber, short headType, byte[] extend) {
        this.magicNumber = magicNumber;
        this.headType = headType;
        this.extend = extend;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        Utils.uint32ToByteStreamLE(magicNumber, stream);
        Utils.uint32ToByteStreamLE(length, stream);
        Utils.uint16ToByteStreamLE(headType, stream);
        if (extend == null) {
            extend = new byte[]{1, 2, 3, 4, 0, 0, 0, 0};
        } else {
            if (extend.length != 8) {
                throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
            }
        }
        stream.write(extend);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        this.magicNumber = byteBuffer.readInt32();
        this.length = byteBuffer.readInt32();
        this.headType = byteBuffer.readShort();
        this.extend = byteBuffer.readBytes(8);
    }


    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public short getHeadType() {
        return headType;
    }

    public void setHeadType(short headType) {
        this.headType = headType;
    }

    public static void main(String[] args) throws IOException {
        NulsMessageHeader header = new NulsMessageHeader(12345678, NulsMessageHeader.NETWORK_MESSAGE);
        byte[] bytes = header.serialize();

        header.parse(new NulsByteBuffer(bytes));
        System.out.println(header.toString());

    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("NulsMessageHeader{ ");
        buffer.append("magicNumber:" + magicNumber + ",");
        buffer.append("length:" + length + ",");
        buffer.append("headType:" + headType + ",");
        if (extend != null && extend.length != 0) {
            buffer.append("extends:" + new String(extend));
        } else {
            buffer.append("extends:null");
        }
        buffer.append("}");
        return buffer.toString();
    }
}

package io.nuls.core.mesasge;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

public class NulsMessageHeader extends BaseNulsData {

    public static final int MESSAGE_HEADER_SIZE = 20;

    private int magicNumber;

    // the NulsMessage length
    private int length;

    //0x01 : networkMessage  //0x02 : eventMessage;
    private short headType;
    public static final short NETWORK_MESSAGE = 1;
    public static final short EVENT_MESSAGE = 2;

    private byte xor;

    //the extend length
    public static final int EXTEND_LENGTH = 9;
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

    public NulsMessageHeader(int magicNumber, short headType, int length, byte xor) {
        this.magicNumber = magicNumber;
        this.headType = headType;
        this.length = length;
        this.xor = xor;
    }

    public NulsMessageHeader(int magicNumber, short headType, int length, byte xor, byte[] extend) {
        this.magicNumber = magicNumber;
        this.headType = headType;
        this.length = length;
        this.xor = xor;
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
        stream.write(xor);
        if (extend == null) {
            extend = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        } else {
            if (extend.length != EXTEND_LENGTH) {
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
        this.xor = byteBuffer.readByte();
        this.extend = byteBuffer.readBytes(EXTEND_LENGTH);
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
        System.out.println("byte.length:" + bytes.length);
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
            buffer.append("extends:");
            for (byte b : extend) {
                buffer.append(b + " ");
            }
        } else {
            buffer.append("extends:null");
        }
        buffer.append("}");
        return buffer.toString();
    }

    public byte getXor() {
        return xor;
    }

    public void setXor(byte xor) {
        this.xor = xor;
    }
}

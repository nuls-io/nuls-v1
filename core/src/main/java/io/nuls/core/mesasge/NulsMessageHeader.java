package io.nuls.core.mesasge;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.mesasge.constant.MessageTypeEnum;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

public class NulsMessageHeader extends NulsData {

    public static final int MESSAGE_HEADER_SIZE = 20;

    private int magicNumber;

    // the message length
    private int length;

    //0x01 : networkMessage  //0x02 : eventMessage;
    public static final short NETWORK_MESSAGE = 1;
    public static final short EVENT_MESSAGE = 2;
    private short msgType;

    private byte[] extend;


    public NulsMessageHeader(int magicNumber, short msgType) {
        this.magicNumber = magicNumber;
        this.msgType = msgType;
    }

    public NulsMessageHeader(int magicNumber, short msgType, byte[] extend) {
        this.magicNumber = magicNumber;
        this.msgType = msgType;
        this.extend = extend;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(magicNumber).encode());
        stream.write(new VarInt(length).encode());
        stream.write(new VarInt(msgType).encode());
        stream.write(extend);
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        this.magicNumber = byteBuffer.readInt32();
        this.length = byteBuffer.readInt32();
        this.msgType = byteBuffer.readShort();
        this.extend = byteBuffer.readBytes(8);
    }

    public short getMsgType() {
        return msgType;
    }

    public void setMsgType(short msgType) {
        this.msgType = msgType;
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
}

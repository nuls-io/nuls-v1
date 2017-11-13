package io.nuls.core.mesasge;

import io.nuls.core.mesasge.constant.MessageTypeEnum;

public class NulsMessage {

    public static final int MAX_SIZE = 0x01000000; //16M

    private NulsMessageHeader header;
    private MessageTypeEnum messageType;
    private byte[] data;

    public NulsMessage(NulsMessageHeader header, MessageTypeEnum messageType, byte[] data) {
        this.header = header;
        this.data = data;
    }

    public NulsMessage(MessageTypeEnum messageType, byte[] data) {
        this.messageType = messageType;
        this.data = data;
    }

    public NulsMessageHeader getHeader() {
        return header;
    }

    public void setHeader(NulsMessageHeader header) {
        this.header = header;
    }

    public MessageTypeEnum getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageTypeEnum messageType) {
        this.messageType = messageType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

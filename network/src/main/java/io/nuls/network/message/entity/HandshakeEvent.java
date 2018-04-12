package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.EventHeader;
import io.nuls.core.event.NoticeData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.constant.NetworkConstant;

import java.io.IOException;

public class HandshakeEvent extends BaseEvent {

    private int handshakeType;


    public HandshakeEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_HANDSHAKE_EVENT);
    }

    public HandshakeEvent(int handshakeType) {
        this();
        this.handshakeType = handshakeType;
    }

    @Override
    public int size() {
        int s = 0;
        s += EventHeader.EVENT_HEADER_LENGTH;
        s += VarInt.sizeOf(handshakeType);
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(getHeader());
        stream.writeVarInt(handshakeType);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.setHeader(byteBuffer.readNulsData(new EventHeader()));
        handshakeType = (int) byteBuffer.readVarInt();
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return null;
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }

    public int getHandshakeType() {
        return handshakeType;
    }

    public void setHandshakeType(int handshakeType) {
        this.handshakeType = handshakeType;
    }
}

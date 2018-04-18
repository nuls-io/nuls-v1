package io.nuls.network.message.entity;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.protocol.event.base.BaseEvent;
import io.nuls.protocol.event.base.EventHeader;
import io.nuls.protocol.event.base.NoticeData;
import io.nuls.protocol.model.BaseNulsData;
import io.nuls.protocol.utils.io.NulsByteBuffer;
import io.nuls.protocol.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

public class HandshakeEvent extends BaseEvent {

    private int handshakeType;

    private int severPort;

    private long bestBlockHeight;

    private String bestBlockHash;

    public HandshakeEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_HANDSHAKE_EVENT);
    }

    public HandshakeEvent(int handshakeType, int severPort, long bestBlockHeight, String bestBlockHash) {
        this();
        this.handshakeType = handshakeType;
        this.severPort = severPort;
        this.bestBlockHeight = bestBlockHeight;
        this.bestBlockHash = bestBlockHash;
    }

    @Override
    public int size() {
        int s = 0;
        s += EventHeader.EVENT_HEADER_LENGTH;
        s += VarInt.sizeOf(handshakeType);
        s += VarInt.sizeOf(severPort);
        s += VarInt.sizeOf(bestBlockHeight);
        s += Utils.sizeOfString(bestBlockHash);
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(getHeader());
        stream.writeVarInt(handshakeType);
        stream.writeVarInt(severPort);
        stream.writeVarInt(bestBlockHeight);
        stream.writeString(bestBlockHash);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.setHeader(byteBuffer.readNulsData(new EventHeader()));
        handshakeType = (int) byteBuffer.readVarInt();
        severPort = (int) byteBuffer.readVarInt();
        bestBlockHeight = byteBuffer.readVarInt();
        bestBlockHash = byteBuffer.readString();
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

    public long getBestBlockHeight() {
        return bestBlockHeight;
    }

    public void setBestBlockHeight(long bestBlockHeight) {
        this.bestBlockHeight = bestBlockHeight;
    }

    public String getBestBlockHash() {
        return bestBlockHash;
    }

    public void setBestBlockHash(String bestBlockHash) {
        this.bestBlockHash = bestBlockHash;
    }

    public int getSeverPort() {
        return severPort;
    }

    public void setSeverPort(int severPort) {
        this.severPort = severPort;
    }
}

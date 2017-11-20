package io.nuls.network.message;

import io.nuls.core.crypto.VarInt;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.network.entity.param.NetworkParam;

import java.io.IOException;

public abstract class NetworkMessage extends NulsMessage {

    protected short msgType;

    public NetworkMessage(NetworkParam network) {
        super(new NetworkMessageHeader(network.packetMagic()), null);
    }

    public NetworkMessage(NetworkParam network, byte[] data) {
        super(new NetworkMessageHeader(network.packetMagic()), data);
    }

    @Override
    public byte[] serialize() throws IOException {
        byte[] value = new byte[MAX_SIZE + data.length + 2];
        byte[] headerBytes = header.serialize();
        System.arraycopy(headerBytes, 0, value, 0, headerBytes.length);

        System.arraycopy(new VarInt(msgType).encode(), 0, value, headerBytes.length, 2);
        System.arraycopy(data, 0, value, headerBytes.length, data.length);
        return value;
    }

    public short getMsgType() {
        return msgType;
    }

    public void setMsgType(short msgType) {
        this.msgType = msgType;
    }
}

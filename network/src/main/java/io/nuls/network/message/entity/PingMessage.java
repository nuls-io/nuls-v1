package io.nuls.network.message.entity;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.NetworkMessage;

import java.io.IOException;
import java.io.OutputStream;

public class PingMessage extends NetworkMessage {

    public PingMessage() {
        this.msgType = NetworkConstant.Network_Ping_Message;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {

    }

}

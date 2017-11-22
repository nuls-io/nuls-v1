package io.nuls.network.message.entity;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.AbstractNetworkMessage;

import java.io.IOException;
import java.io.OutputStream;

public class PongMessage extends AbstractNetworkMessage {

    public PongMessage() {
        this.type = NetworkConstant.Network_Pong_Mesaage;
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

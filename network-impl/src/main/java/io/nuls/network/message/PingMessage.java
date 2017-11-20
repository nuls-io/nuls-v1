package io.nuls.network.message;

import io.nuls.network.entity.param.NetworkParam;

public class PingMessage extends NetworkMessage {

    public PingMessage(NetworkParam network) {
        super(network);
    }
}

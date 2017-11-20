package io.nuls.network.message;

import io.nuls.network.entity.param.NetworkParam;

public class PongMessage extends NetworkMessage {

    public PongMessage(NetworkParam network) {
        super(network);
    }
}

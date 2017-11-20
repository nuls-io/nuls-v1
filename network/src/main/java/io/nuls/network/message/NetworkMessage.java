package io.nuls.network.message;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.network.entity.param.NetworkParam;

public abstract class NetworkMessage extends NulsMessage {

    public NetworkMessage(NetworkParam network) {
        super(new NetworkMessageHeader(network.packetMagic()), null);
    }

    public NetworkMessage(NetworkParam network, byte[] data) {
        super(new NetworkMessageHeader(network.packetMagic()), data);
    }
}

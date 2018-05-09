package io.nuls.network.protocol.message;

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.protostuff.Tag;

public class NetworkMessageBody extends BaseNulsData {

    @Tag(1)
    private int handshakeType;
    @Tag(2)
    private int severPort;
    @Tag(3)
    private long bestBlockHeight;
    @Tag(4)
    private NulsDigestData bestBlockHash;


    public NetworkMessageBody() {

    }

    public NetworkMessageBody(int handshakeType, int severPort, long bestBlockHeight, NulsDigestData bestBlockHash) {
        this.handshakeType = handshakeType;
        this.severPort = severPort;
        this.bestBlockHeight = bestBlockHeight;
        this.bestBlockHash = bestBlockHash;
    }
}

package io.nuls.network.message;

import io.nuls.core.mesasge.NulsMessageHeader;

public class NetworkMessageHeader extends NulsMessageHeader {
    public NetworkMessageHeader(int magicNumber, short msgType) {
        super(magicNumber, msgType);
    }
}

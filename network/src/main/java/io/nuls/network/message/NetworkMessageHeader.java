package io.nuls.network.message;

import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;

public class NetworkMessageHeader extends NulsMessageHeader {
    public NetworkMessageHeader(int magicNumber) {
        super(magicNumber, NulsMessageHeader.NETWORK_MESSAGE);
    }
}

package io.nuls.network.message;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;

public abstract class BaseNetworkMessage extends NulsMessage {

    public BaseNetworkMessage() {
        super(null, null);
    }
}

package io.nuls.network.event;

import io.nuls.core.event.NulsEvent;
import io.nuls.core.event.NulsEventHeader;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public abstract class BaseNetworkEvent extends NulsEvent {
    public BaseNetworkEvent(NulsEventHeader header) {
        super(header);
    }
}

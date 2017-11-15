package io.nuls.consensus.event;

import io.nuls.core.event.NulsEvent;
import io.nuls.core.event.NulsEventHeader;

/**
 * Created by Niels on 2017/11/7.
 *
 */
public abstract class BaseConsensusEvent extends NulsEvent{
    public BaseConsensusEvent(NulsEventHeader header) {
        super(header);
    }
}

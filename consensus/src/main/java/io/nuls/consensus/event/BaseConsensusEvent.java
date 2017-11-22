package io.nuls.consensus.event;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.event.NulsEventHeader;

/**
 *
 * @author Niels
 * @date 2017/11/7
 *
 */
public abstract class BaseConsensusEvent extends BaseNulsEvent{
    public BaseConsensusEvent(NulsEventHeader header) {
        super(header);
    }
}

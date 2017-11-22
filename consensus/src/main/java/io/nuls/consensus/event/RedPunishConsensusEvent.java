package io.nuls.consensus.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 *
 * @author Niels
 * @date 2017/11/13
 *
 */
public class RedPunishConsensusEvent extends BaseConsensusEvent{
    public RedPunishConsensusEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) {
        return null;
    }


}
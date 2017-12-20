package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 *
 * @author Niels
 * @date 2017/11/13
 *
 */
//todo
public class RedPunishConsensusEvent extends BaseConsensusEvent{
    public RedPunishConsensusEvent( ) {
        super(PocConsensusConstant.EVENT_TYPE_RED_PUNISH);
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) {
        return null;
    }


}
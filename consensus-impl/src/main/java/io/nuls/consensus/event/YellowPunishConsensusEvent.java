package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class YellowPunishConsensusEvent extends BaseConsensusEvent {
    public YellowPunishConsensusEvent() {
        super(PocConsensusConstant.EVENT_TYPE_YELLOW_PUNISH);
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) {
        //todo
        return null;
    }


}
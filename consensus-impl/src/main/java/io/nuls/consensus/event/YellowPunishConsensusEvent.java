package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/13
 */
//todo
public class YellowPunishConsensusEvent extends BaseConsensusEvent {
    public YellowPunishConsensusEvent() {
        super(PocConsensusConstant.EVENT_TYPE_YELLOW_PUNISH);
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) {
        //todo
        return null;
    }
    @Override
    public Object copy() {
        try {
            return this.clone();
        } catch (CloneNotSupportedException e) {
            Log.error(e);
            return null;
        }
    }

}
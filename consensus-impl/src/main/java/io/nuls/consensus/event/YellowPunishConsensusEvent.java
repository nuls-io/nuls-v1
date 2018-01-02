package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class YellowPunishConsensusEvent extends BaseConsensusEvent<YellowPunishTransaction> {
    public YellowPunishConsensusEvent() {
        super(PocConsensusConstant.EVENT_TYPE_YELLOW_PUNISH);
    }

    @Override
    protected YellowPunishTransaction parseEventBody(NulsByteBuffer byteBuffer) {
        YellowPunishTransaction tx = new YellowPunishTransaction();
        try {
            tx.parse(byteBuffer);
        } catch (NulsException e) {
            Log.error(e);
        }
        return tx;
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
package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 *
 * @author Niels
 * @date 2017/11/13
 *
 */
public class RedPunishConsensusEvent extends BaseConsensusEvent<RedPunishTransaction>{
    public RedPunishConsensusEvent( ) {
        super(PocConsensusConstant.EVENT_TYPE_RED_PUNISH);
    }

    @Override
    protected RedPunishTransaction parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new RedPunishTransaction());
    }


}
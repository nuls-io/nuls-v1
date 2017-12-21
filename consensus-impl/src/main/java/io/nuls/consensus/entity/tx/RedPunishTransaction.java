package io.nuls.consensus.entity.tx;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RedPunishTransaction extends Transaction {
    public RedPunishTransaction( ) {
        super(PocConsensusConstant.EVENT_TYPE_RED_PUNISH);
    }

    @Override
    protected BaseNulsData parseTxData(NulsByteBuffer byteBuffer) {
        // todo auto-generated method stub(niels)
        return null;
    }
}

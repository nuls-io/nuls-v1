package io.nuls.consensus.entity.tx;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.tx.AbstractConsensusTransaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class YellowPunishTransaction extends AbstractConsensusTransaction {
    public YellowPunishTransaction( ) {
        super(PocConsensusConstant.EVENT_TYPE_YELLOW_PUNISH);
    }
}

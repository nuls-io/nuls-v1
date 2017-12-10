package io.nuls.consensus.entity.tx;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.tx.AbstractConsensusTransaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RedPunishTransaction extends AbstractConsensusTransaction {
    public RedPunishTransaction( ) {
        super(PocConsensusConstant.EVENT_TYPE_RED_PUNISH);
    }
}

package io.nuls.consensus.entity;

import io.nuls.consensus.constant.POCConsensusConstant;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class YellowPunishTransaction extends AbstractConsensusTransaction {
    public YellowPunishTransaction( ) {
        super(POCConsensusConstant.EVENT_TYPE_YELLOW_PUNISH);
    }
}

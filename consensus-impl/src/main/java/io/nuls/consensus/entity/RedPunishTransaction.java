package io.nuls.consensus.entity;

import io.nuls.consensus.constant.POCConsensusConstant;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RedPunishTransaction extends AbstractConsensusTransaction {
    public RedPunishTransaction( ) {
        super(POCConsensusConstant.EVENT_TYPE_RED_PUNISH);
    }
}

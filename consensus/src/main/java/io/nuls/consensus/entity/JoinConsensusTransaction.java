package io.nuls.consensus.entity;

import io.nuls.consensus.constant.ConsensusConstant;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class JoinConsensusTransaction extends AbstractConsensusTransaction<ConsensusAccount> {
    public JoinConsensusTransaction() {
        super(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
    }
}

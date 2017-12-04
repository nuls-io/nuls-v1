package io.nuls.consensus.entity;

import io.nuls.consensus.constant.ConsensusConstant;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class ExitConsensusTransaction extends AbstractConsensusTransaction<ExitConsensusTransaction> {
    public ExitConsensusTransaction() {
        super(ConsensusConstant.TX_TYPE_EXIT_CONSENSUS);
    }
}

package io.nuls.consensus.tx;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.entity.ConsensusAccount;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class JoinConsensusTransaction extends AbstractConsensusTransaction<ConsensusAccount> {
    public JoinConsensusTransaction() {
        super(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
    }
}

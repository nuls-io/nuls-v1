package io.nuls.consensus.tx;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.entity.ConsensusAccount;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class ExitConsensusTransaction extends AbstractConsensusTransaction<ConsensusAccount> {
    public ExitConsensusTransaction() {
        super(ConsensusConstant.TX_TYPE_EXIT_CONSENSUS);
    }
}

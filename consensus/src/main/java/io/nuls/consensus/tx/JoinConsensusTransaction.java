package io.nuls.consensus.tx;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class JoinConsensusTransaction extends AbstractConsensusTransaction<Consensus> {
    public JoinConsensusTransaction() {
        super(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
    }
    @Override
    protected Consensus parseBody(NulsByteBuffer byteBuffer) {
        Consensus ca = new Consensus();
        ca.parse(byteBuffer);
        return ca;
    }
}

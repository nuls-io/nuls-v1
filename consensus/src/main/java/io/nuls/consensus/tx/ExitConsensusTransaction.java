package io.nuls.consensus.tx;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * txData:the hash of join consensus transaction
 *
 * @author Niels
 * @date 2017/12/4
 */
public class ExitConsensusTransaction extends AbstractConsensusTransaction<NulsDigestData> {
    public ExitConsensusTransaction() {
        super(ConsensusConstant.TX_TYPE_EXIT_CONSENSUS);
    }

    @Override
    protected NulsDigestData parseBody(NulsByteBuffer byteBuffer) {
        NulsDigestData ca = new NulsDigestData();
        ca.parse(byteBuffer);
        return ca;
    }
}

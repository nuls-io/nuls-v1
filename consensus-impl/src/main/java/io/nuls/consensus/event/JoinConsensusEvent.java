package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class JoinConsensusEvent extends BaseConsensusEvent<PocJoinConsensusTransaction> {

    public JoinConsensusEvent() {
        super(PocConsensusConstant.EVENT_TYPE_JOIN_CONSENSUS);
    }

    @Override
    protected PocJoinConsensusTransaction parseEventBody(NulsByteBuffer byteBuffer) {
        PocJoinConsensusTransaction tx = new PocJoinConsensusTransaction();
        tx.parse(byteBuffer);
        return tx;
    }

}

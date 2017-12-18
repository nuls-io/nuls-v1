package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.tx.JoinConsensusTransaction;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class JoinConsensusEvent extends BaseConsensusEvent<JoinConsensusTransaction> {

    public JoinConsensusEvent() {
        super(ConsensusEventType.JOIN);
    }

    @Override
    protected JoinConsensusTransaction parseEventBody(NulsByteBuffer byteBuffer) {
        JoinConsensusTransaction tx = new JoinConsensusTransaction();
        tx.parse(byteBuffer);
        return tx;
    }

}

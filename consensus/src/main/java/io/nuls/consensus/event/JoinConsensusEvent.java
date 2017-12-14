package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.tx.JoinConsensusTransaction;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

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

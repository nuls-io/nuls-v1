package io.nuls.consensus.event;

import io.nuls.consensus.constant.POCConsensusConstant;
import io.nuls.consensus.entity.RegisterAgentTransaction;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RegisterAgentEvent extends BaseConsensusEvent<RegisterAgentTransaction> {
    public RegisterAgentEvent() {
        super(POCConsensusConstant.EVENT_TYPE_REGISTER_AGENT);
    }

    @Override
    protected RegisterAgentTransaction parseEventBody(NulsByteBuffer byteBuffer) {
        RegisterAgentTransaction tx = new RegisterAgentTransaction();
        tx.parse(byteBuffer);
        return tx;
    }
}

package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RegisterAgentEvent extends BaseConsensusEvent<RegisterAgentTransaction> {
    public RegisterAgentEvent() {
        super(PocConsensusConstant.EVENT_TYPE_REGISTER_AGENT);
    }

    @Override
    protected RegisterAgentTransaction parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new RegisterAgentTransaction());
    }

}

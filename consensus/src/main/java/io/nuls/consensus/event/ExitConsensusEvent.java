package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/7
 */
//todo
public class ExitConsensusEvent extends BaseConsensusEvent<ConsensusAccount> {

    public ExitConsensusEvent() {
        super(ConsensusEventType.EXIT);
    }

    @Override
    protected ConsensusAccount parseEventBody(NulsByteBuffer byteBuffer) {
        ConsensusAccount member = new ConsensusAccount();
        member.parse(byteBuffer);
        return member;
    }

}

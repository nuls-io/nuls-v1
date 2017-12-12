package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.ConsensusMember;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/7
 */
//todo
public class ExitConsensusEvent extends BaseConsensusEvent<ConsensusMember> {

    public ExitConsensusEvent() {
        super(ConsensusEventType.EXIT);
    }

    @Override
    protected ConsensusMember parseEventBody(NulsByteBuffer byteBuffer) {
        ConsensusMember member = new ConsensusMember();
        member.parse(byteBuffer);
        return member;
    }

}

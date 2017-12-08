package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.ConsensusMember;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/11/7
 */
//todo
public class JoinConsensusEvent extends BaseConsensusEvent<ConsensusMember> {

    public JoinConsensusEvent() {
        super(ConsensusEventType.JOIN);
    }

    @Override
    protected ConsensusMember parseEventBody(NulsByteBuffer byteBuffer) {
        ConsensusMember member = new ConsensusMember();
        member.parse(byteBuffer);
        return member;
    }


}

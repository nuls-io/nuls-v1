package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class GetBlockEvent extends BaseConsensusEvent{
    public GetBlockEvent( ) {
        super(ConsensusEventType.GET_BLOCK);
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) {
        //todo
        return null;
    }


}
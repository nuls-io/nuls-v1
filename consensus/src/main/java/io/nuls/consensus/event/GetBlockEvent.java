package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * get block by height.
 * @author Niels
 * @date 2017/11/13
 */
public class GetBlockEvent extends BaseConsensusEvent<BasicTypeData<Long>>{
    public GetBlockEvent( ) {
        super(ConsensusEventType.GET_BLOCK);
    }

    @Override
    protected BasicTypeData<Long> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
       return new BasicTypeData<>(byteBuffer);
    }

}
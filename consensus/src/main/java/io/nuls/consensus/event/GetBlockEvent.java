package io.nuls.consensus.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class GetBlockEvent extends BaseConsensusEvent{
    public GetBlockEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) {
        //todo
        return null;
    }


}
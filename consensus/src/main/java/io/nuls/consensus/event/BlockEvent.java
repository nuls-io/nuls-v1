package io.nuls.consensus.event;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class BlockEvent extends BaseConsensusEvent{
    public BlockEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected NulsData parseEventBody(NulsByteBuffer byteBuffer) {
        return null;
    }


}

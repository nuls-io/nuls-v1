package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class BlockEvent extends BaseConsensusEvent<Block> {
    public BlockEvent() {
        super(ConsensusEventType.BLOCK);
    }

    @Override
    protected Block parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new Block());
    }


}

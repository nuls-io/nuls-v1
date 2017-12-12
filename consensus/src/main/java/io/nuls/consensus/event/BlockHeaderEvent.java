package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class BlockHeaderEvent extends BaseConsensusEvent<BlockHeader> {
    public BlockHeaderEvent() {
        super(ConsensusEventType.BLOCK_HEADER);
    }

    @Override
    protected BlockHeader parseEventBody(NulsByteBuffer byteBuffer) {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.parse(byteBuffer);
        return blockHeader;
    }

}

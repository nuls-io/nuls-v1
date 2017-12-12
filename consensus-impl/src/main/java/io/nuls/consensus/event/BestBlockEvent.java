package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BestBlockEvent extends BaseConsensusEvent<BlockHeader> {
    public BestBlockEvent() {
        super(PocConsensusConstant.EVENT_TYPE_ASK_BEST_BLOCK);
    }

    @Override
    protected BlockHeader parseEventBody(NulsByteBuffer byteBuffer) {
        BlockHeader header = new BlockHeader();
        header.parse(byteBuffer);
        return header;
    }
}

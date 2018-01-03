package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class BlockHeaderEvent extends BaseConsensusEvent<BlockHeader> {
    public BlockHeaderEvent() {
        super(ConsensusEventType.BLOCK_HEADER);
    }

    public BlockHeaderEvent(BlockHeader header) {
        this();
        this.setEventBody(header);
    }

    @Override
    protected BlockHeader parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new BlockHeader());
    }

}

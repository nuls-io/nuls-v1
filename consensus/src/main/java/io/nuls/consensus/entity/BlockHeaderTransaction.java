package io.nuls.consensus.entity;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.core.chain.entity.BlockHeader;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class BlockHeaderTransaction extends AbstractConsensusTransaction {

    private BlockHeader blockHeader;

    public BlockHeaderTransaction() {
        super(ConsensusConstant.TX_TYPE_BLOCK_HEADER);
    }

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }
}

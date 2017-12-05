package io.nuls.consensus.tx;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.core.chain.entity.Block;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class BlockTransaction extends AbstractConsensusTransaction {
    private Block block;
    public BlockTransaction() {
        super(ConsensusConstant.TX_TYPE_BLOCK);
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}

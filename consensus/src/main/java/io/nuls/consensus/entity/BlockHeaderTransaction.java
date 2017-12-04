package io.nuls.consensus.entity;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.core.chain.entity.BlockHeader;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class BlockHeaderTransaction extends AbstractConsensusTransaction<BlockHeader> {
    public BlockHeaderTransaction() {
        super(ConsensusConstant.TX_TYPE_BLOCK_HEADER);
    }
}

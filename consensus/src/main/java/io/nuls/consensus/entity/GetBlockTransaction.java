package io.nuls.consensus.entity;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.core.chain.entity.Block;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class GetBlockTransaction extends AbstractConsensusTransaction {
    public GetBlockTransaction() {
        super(ConsensusConstant.TX_TYPE_GET_BLOCK);
    }
}

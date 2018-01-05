package io.nuls.db.dao;

import io.nuls.core.chain.entity.Result;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.TransactionPo;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/20
 */
public interface ConsensusDataService {

    Result blockPersistence(BlockPo blockPo, List<TransactionPo> txPoList);
}

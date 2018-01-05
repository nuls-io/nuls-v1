package io.nuls.db.dao;

import io.nuls.db.entity.BlockPo;


/**
 * @author zhouwei
 * @date 2017/9/29
 */
public interface BlockDataService extends BaseDataService<String, BlockPo> {

    BlockPo getBlock(long height);

    long queryMaxHeight();

    BlockPo getHighestBlock();

    BlockPo getBlockByHash(String hash);

    int count(String address, long roundStart, long roundEnd);

}

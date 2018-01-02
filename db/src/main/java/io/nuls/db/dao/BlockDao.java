package io.nuls.db.dao;

import io.nuls.db.entity.BlockPo;


/**
 * @author zhouwei
 * @date 2017/9/29
 */
public interface BlockDao extends BaseDao<String, BlockPo> {

    BlockPo getBlockByHeight(long height);

    long queryMaxHeight();

    BlockPo getHighestBlock();

    BlockPo getBlockByHash(String hash);

    int deleteAll();

    int count(String address, long roundStart, long roundEnd);

    int queryCount(String address, int txType);
}

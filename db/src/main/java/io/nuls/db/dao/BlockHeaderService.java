package io.nuls.db.dao;

import io.nuls.db.entity.BlockHeaderPo;

import java.util.List;


/**
 * @author zhouwei
 * @date 2017/9/29
 */
public interface BlockHeaderService extends BaseDataService<String, BlockHeaderPo> {

    BlockHeaderPo getHeader(long height);

    BlockHeaderPo getHeader(String hash);

    long getBestHeight();

    BlockHeaderPo getBestBlockHeader();

    List<BlockHeaderPo> getHeaderList(long startHeight, long endHeight);


}

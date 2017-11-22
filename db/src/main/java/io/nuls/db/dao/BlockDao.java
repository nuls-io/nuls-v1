package io.nuls.db.dao;

import io.nuls.db.entity.BlockPo;

import java.util.List;


/**
 * Created by zhouwei on 2017/9/29.
 */
public interface BlockDao extends BaseDao<String,BlockPo>{

    List<BlockPo> getList(Integer pageNum, Integer pageSize);

    Long getCount();

}

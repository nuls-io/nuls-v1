package io.nuls.db.dao;

import io.nuls.db.entity.BlockPo;

import java.util.List;


/**
 * Created by zoro on 2017/9/29.
 */
public interface BlockDao extends BaseDao<BlockPo,String>{

    List<BlockPo> getList(Integer pageNum, Integer pageSize);

}

package io.nuls.db.intf;

import io.nuls.db.entity.Block;

import java.util.List;


/**
 * Created by zoro on 2017/9/29.
 */
public interface IBlockStore extends IStore<Block,String>{

    List<Block> getList(Integer pageNum, Integer pageSize);

}

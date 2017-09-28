package io.nuls.db.impl;

import io.nuls.db.dao.mybatis.BlockMapper;
import io.nuls.db.entity.Block;
import io.nuls.db.intf.IStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by win10 on 2017/9/26.
 */
@Service("blockStoreService")
public class BlockStoreService implements IStoreService<Block,String> {

    @Autowired
    private BlockMapper blockMapper;

    @Override
    public void save(Block b) {
        blockMapper.insert(b);
    }

    @Override
    public Block getByKey(String hash) {
        return blockMapper.selectByPrimaryKey(hash);
    }
}

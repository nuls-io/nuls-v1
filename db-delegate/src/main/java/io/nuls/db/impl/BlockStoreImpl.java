package io.nuls.db.impl;

import io.nuls.db.dao.mybatis.BlockMapper;
import io.nuls.db.entity.Block;
import io.nuls.db.intf.IBlockStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by win10 on 2017/9/29.
 */
@Service("blockStore")
public class BlockStoreImpl implements IBlockStore {

    @Autowired
    private BlockMapper blockMapper;

    @Override
    public int save(Block block) {
        return blockMapper.insert(block);
    }

    @Override
    public int saveBatch(List<Block> list) {
        return 0;
    }

    @Override
    public int update(Block block, boolean selective) {
        if(!selective) {
            return blockMapper.updateByPrimaryKey(block);
        }else {
            return blockMapper.updateByPrimaryKeySelective(block);
        }
    }

    @Override
    public int deleteByKey(String key) {
        return blockMapper.deleteByPrimaryKey(key);
    }

    @Override
    public int truncate() {
        return blockMapper.truncate();
    }

    @Override
    public Block getByKey(String key) {
        return blockMapper.selectByPrimaryKey(key);
    }

    @Override
    public List<Block> getList() {
        return null;
    }

    @Override
    public long count() {
        return blockMapper.count();
    }

    @Override
    public int exist() {
        return 0;
    }

    public void setBlockMapper(BlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }
}

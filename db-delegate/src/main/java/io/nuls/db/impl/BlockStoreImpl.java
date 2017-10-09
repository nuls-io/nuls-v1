package io.nuls.db.impl;

import io.nuls.db.DBConstant;
import io.nuls.db.DBException;
import io.nuls.db.dao.mybatis.BlockMapper;
import io.nuls.db.dao.mybatis.util.Searchable;
import io.nuls.db.entity.Block;
import io.nuls.db.intf.IBlockStore;
import io.nuls.util.constant.ErrorCode;
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
        if(list == null || list.size() == 0) {
            throw new DBException(ErrorCode.DB_SAVE_CANNOT_NULL);
        }
        if(list.size() > DBConstant.DB_SAVE_LIMIT) {
            throw new DBException(ErrorCode.DB_SAVE_BATCH_LIMIT_OVER);
        }
        return blockMapper.insertBatch(list);
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
        return blockMapper.count(new Searchable());
    }

    @Override
    public int exist() {
        return 0;
    }

    public void setBlockMapper(BlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }
}

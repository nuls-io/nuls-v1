package io.nuls.db.dao.impl;

import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.mapper.BlockMapper;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.mybatis.util.Searchable;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

/**
 * Created by win10 on 2017/9/29.
 */
public class BlockDaoImpl implements BlockDao {

    private BlockMapper blockMapper;


    private SqlSessionFactory sqlSessionFactory;


    public BlockDaoImpl(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public int save(BlockPo block) {
        return blockMapper.insert(block);
    }

    @Override
    public int saveBatch(List<BlockPo> list) {
        if(list == null || list.size() == 0) {
//            throw new DBException()
        }
        return 0;
    }

    @Override
    public int update(BlockPo block, boolean selective) {

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
    public BlockPo getByKey(String key) {
        return blockMapper.selectByPrimaryKey(key);
    }

    public List<BlockPo> getList() {
        return null;
    }

    public long count() {
        return blockMapper.count(new Searchable());
    }

    public int exist() {
        return 0;
    }

    public void setBlockMapper(BlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    @Override
    public List<BlockPo> getList(Integer pageNum, Integer pageSize) {
        return null;
    }
}

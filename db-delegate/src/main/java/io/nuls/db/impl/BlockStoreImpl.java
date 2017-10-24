package io.nuls.db.impl;

import io.nuls.db.DBException;
import io.nuls.db.dao.mybatis.BlockMapper;
import io.nuls.db.dao.mybatis.session.NulsSqlSession;
import io.nuls.db.dao.mybatis.session.NulsSqlSessionFactory;
import io.nuls.db.dao.mybatis.util.Searchable;
import io.nuls.db.entity.Block;
import io.nuls.db.entity.BlockHeader;
import io.nuls.db.intf.IBlockStore;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

/**
 * Created by win10 on 2017/9/29.
 */
public class BlockStoreImpl extends BaseStore implements IBlockStore {

    private BlockMapper blockMapper;

    public BlockStoreImpl(NulsSqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.blockMapper = sqlSessionFactory.openSession().getMapper(BlockMapper.class);
    }

    @Override
    public int save(Block block) {
        return blockMapper.insert(block);
    }

    @Override
    public int saveBatch(List<Block> list) {
        if(list == null || list.size() == 0) {
//            throw new DBException()
        }
        return 0;
    }

    @Override
    public int update(Block block) {
        NulsSqlSession session = sqlSessionFactory.openSession(false);
        session.setOpenSessionClass(this);
        return blockMapper.updateByPrimaryKey(block);
    }

    @Override
    public int updateSelective(Block block) {
        NulsSqlSession session = sqlSessionFactory.openSession(false);
        session.setOpenSessionClass(this);
        return blockMapper.updateByPrimaryKeySelective(block);
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

    public List<Block> getList() {
        return null;
    }

    /**
     *
     * @return
     */
    public long getAllCount() {
        return count(new Searchable());
    }

    public long getBestHeight() {
        return 0;
    }

    public String getBestHash() {
        return null;
    }

    @Override
    public Block getBlockByHeight(long height) {
        return null;
    }

    @Override
    public BlockHeader getBlockHeaderByKey(String hash) {
        return null;
    }

    @Override
    public BlockHeader getBlockHeaderByKey(byte[] hash) {
        return null;
    }

    public BlockHeader getBlockHeaderByHash(String hash) {
        return null;
    }

    @Override
    public BlockHeader getBlockHeaderByHeight(long height) {
        return null;
    }

    public long count(Searchable searchable) {
        return blockMapper.count(searchable);
    }

    public int exist() {
        return 0;
    }

    public void setBlockMapper(BlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    @Override
    public List<Block> getList(Integer pageNum, Integer pageSize) {
        NulsSqlSession session = sqlSessionFactory.openSession(false);
        session.setOpenSessionClass(this);

        return blockMapper.selectList(new Searchable());
    }

    @Override
    public Block getBestBlock() {
        return null;
    }

    @Override
    public Block getByKey(byte[] hash) {
        return null;
    }
}

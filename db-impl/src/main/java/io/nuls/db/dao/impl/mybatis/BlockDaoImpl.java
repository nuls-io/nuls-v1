package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.impl.mybatis.mapper.BlockMapper;
import io.nuls.db.dao.impl.mybatis.session.SessionAnnotation;
import io.nuls.db.entity.BlockPo;

import java.util.List;

/**
 *
 * @author v.chou
 * @date 2017/9/29
 */
public class BlockDaoImpl extends BaseDaoImpl<BlockMapper,String,BlockPo> implements BlockDao {

    public BlockDaoImpl() {
        super(BlockMapper.class);
    }

    private BlockMapper blockMapper;

    @Override
    @SessionAnnotation
    public int save(BlockPo blockPo) {
        blockMapper = getMapper();
        return blockMapper.insert(blockPo);
    }

    @Override
    public int saveBatch(List<BlockPo> list) {
        //todo
        return 0;
    }

    @Override
    @SessionAnnotation
    public int update(BlockPo blockPo) {
        blockMapper = getMapper();
        return blockMapper.updateByPrimaryKey(blockPo);
    }

    @Override
    public int updateSelective(BlockPo blockPo) {
        //todo
        return 0;
    }

    @Override
    public BlockPo getByKey(String key) {
        blockMapper = getMapper();
        return blockMapper.selectByPrimaryKey(key);
    }

    @Override
    public int deleteByKey(String s) {
        //todo
        return 0;
    }

    @Override
    public List<BlockPo> listAll() {
        //todo
        return null;
    }

    @Override
    public List<BlockPo> getList(Integer pageNum, Integer pageSize) {
        //todo
        return null;
    }

    @Override
    public Long getCount() {
        //todo
        return null;
    }

}

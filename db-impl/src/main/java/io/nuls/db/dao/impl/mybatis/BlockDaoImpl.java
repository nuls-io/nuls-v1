package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.impl.mybatis.mapper.BlockMapper;
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


    @Override
    public List<BlockPo> getList(Integer pageNum, Integer pageSize) {
        return null;
    }

}

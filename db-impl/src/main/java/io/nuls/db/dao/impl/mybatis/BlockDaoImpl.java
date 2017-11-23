package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.impl.mybatis.mapper.BlockMapper;
import io.nuls.db.dao.impl.mybatis.params.BlockSearchParams;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.BlockPo;

import java.util.List;
import java.util.Map;

/**
 * @author v.chou
 * @date 2017/9/29
 */
public class BlockDaoImpl extends BaseDaoImpl<BlockMapper, String, BlockPo> implements BlockDao {

    public BlockDaoImpl() {
        super(BlockMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return new BlockSearchParams(params);
    }
}

package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.impl.mybatis.mapper.BlockMapper;
import io.nuls.db.dao.impl.mybatis.params.BlockSearchParams;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.BlockPo;

import java.util.HashMap;
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

    @Override
    public BlockPo getBlockByHeight(long height) {
        // todo auto-generated method stub(niels)
        Map<String, Object> params = new HashMap<>();
        params.put(BlockSearchParams.SEARCH_FIELD_HEIGHT,height);
        List<BlockPo> list = this.searchList(params);
        if(null==list||list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

    @Override
    public long queryMaxHeight() {
        // todo auto-generated method stub(niels)
        return 0;
    }

    @Override
    public BlockPo getHighestBlock() {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public BlockPo getBlockByHash(String hash) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public int deleteAll() {
        // todo auto-generated method stub(niels)
        return 0;
    }
}

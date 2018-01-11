package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.BlockHeaderService;
import io.nuls.db.dao.impl.mybatis.mapper.BlockMapper;
import io.nuls.db.dao.impl.mybatis.params.BlockSearchParams;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.BlockHeaderPo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author v.chou
 * @date 2017/9/29
 */
public class BlockDaoImpl extends BaseDaoImpl<BlockMapper, String, BlockHeaderPo> implements BlockHeaderService {

    public BlockDaoImpl() {
        super(BlockMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return new BlockSearchParams(params);
    }

    @Override
    public BlockHeaderPo getHeader(long height) {
        Map<String, Object> params = new HashMap<>();
        params.put(BlockSearchParams.SEARCH_FIELD_HEIGHT,height);
        List<BlockHeaderPo> list = this.getList(params);
        if(null==list||list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

    @Override
    public long getBestHeight() {
        // todo auto-generated method stub(niels)
        return 0;
    }

    @Override
    public BlockHeaderPo getBestBlockHeader() {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public BlockHeaderPo getHeader(String hash) {
        // todo auto-generated method stub(niels)
        return null;
    }


    @Override
    public int getCount(String address, long roundStart, long roundEnd) {
        // todo auto-generated method stub(niels)
        return 0;
    }

    @Override
    public List<BlockHeaderPo> getHeaderList(long startHeight, long endHeight) {
        // todo auto-generated method stub(niels)
        return null;
    }

}

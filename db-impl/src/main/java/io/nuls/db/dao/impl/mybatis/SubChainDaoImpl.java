package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.SubChainDataService;
import io.nuls.db.dao.impl.mybatis.mapper.SubChainMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.SubChainPo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class SubChainDaoImpl extends BaseDaoImpl<SubChainMapper, String, SubChainPo> implements SubChainDataService {
    public SubChainDaoImpl() {
        super(SubChainMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }
}

package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.DelegateDataService;
import io.nuls.db.dao.impl.mybatis.mapper.DelegateMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.DelegatePo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class DelegateDaoImpl extends BaseDaoImpl<DelegateMapper, String, DelegatePo> implements DelegateDataService {
    public DelegateDaoImpl() {
        super(DelegateMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return null;
    }
}

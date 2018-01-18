package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.DelegateDataService;
import io.nuls.db.dao.impl.mybatis.mapper.DelegateMapper;
import io.nuls.db.dao.impl.mybatis.params.DelegateSearchParams;
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
        return new DelegateSearchParams(params);
    }

    @Override
    public int deleteByAgentAddress(String address) {
        return this.getMapper().deleteByAgentAddress(address);
    }

    @Override
    public int updateSelective(DelegatePo po) {
        return this.getMapper().updateByPrimaryKeySelective(po);
    }

    @Override
    public int updateSelectiveByAgentAddress(DelegatePo po) {
        return this.getMapper().updateSelectiveByAgentAddress(po);
    }
}

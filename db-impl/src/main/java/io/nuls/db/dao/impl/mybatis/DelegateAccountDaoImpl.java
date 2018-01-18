package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.DelegateAccountDataService;
import io.nuls.db.dao.impl.mybatis.mapper.DelegateAccountMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.DelegateAccountPo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class DelegateAccountDaoImpl extends BaseDaoImpl<DelegateAccountMapper, String, DelegateAccountPo> implements DelegateAccountDataService {
    public DelegateAccountDaoImpl() {
        super(DelegateAccountMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return null;
    }

    @Override
    public int updateSelective(DelegateAccountPo po) {
        return getMapper().updateByPrimaryKeySelective(po);
    }
}

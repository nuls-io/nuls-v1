package io.nuls.db.dao.impl.mybatis;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.db.dao.AccountDataService;
import io.nuls.db.dao.impl.mybatis.mapper.AccountMapper;
import io.nuls.db.dao.impl.mybatis.params.AccountSearchParams;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.AccountPo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/21
 */
public class AccountDaoImpl extends BaseDaoImpl<AccountMapper, String, AccountPo> implements AccountDataService {

    public AccountDaoImpl() {
        super(AccountMapper.class);
    }

    @Override
    public AccountPo loadByAddress(String address) {
        Map<String, Object> params = new HashMap<>();
        params.put(AccountSearchParams.SEARCH_FIELD_ADDRESS, address);
        List<AccountPo> list = this.getList(params);
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new NulsRuntimeException(ErrorCode.DB_DATA_ERROR, "data error");
        }
        return list.get(0);
    }

    @Override
    public int updateAlias(AccountPo po) {
        return getMapper().updateAlias(po);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return new AccountSearchParams(params);
    }
}

package io.nuls.db.dao.impl.mybatis.util;

import io.nuls.db.dao.AccountDao;
import io.nuls.db.dao.impl.mybatis.BaseDaoImpl;
import io.nuls.db.entity.AccountPo;

import java.util.List;

/**
 * Created by Niels on 2017/11/21.
 */
public class AccountDaoImpl extends BaseDaoImpl implements AccountDao {
    @Override
    public boolean setAlias(String id, String alias) {
        //todo
        return false;
    }

    @Override
    public int save(AccountPo accountPo) {
        //todo
        return 0;
    }

    @Override
    public int saveBatch(List<AccountPo> list) {
        //todo
        return 0;
    }

    @Override
    public int update(AccountPo accountPo) {
        //todo
        return 0;
    }

    @Override
    public int updateSelective(AccountPo accountPo) {
        //todo
        return 0;
    }

    @Override
    public AccountPo getByKey(String key) {
        //todo
        return null;
    }

    @Override
    public int deleteByKey(String key) {
        //todo
        return 0;
    }

    @Override
    public List<AccountPo> listAll() {
        //todo
        return null;
    }
}

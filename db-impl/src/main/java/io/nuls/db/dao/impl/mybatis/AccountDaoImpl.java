package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.AccountDao;
import io.nuls.db.dao.impl.mybatis.BaseDaoImpl;
import io.nuls.db.dao.impl.mybatis.mapper.AccountMapper;
import io.nuls.db.entity.AccountPo;

import java.util.List;

/**
 * Created by Niels on 2017/11/21.
 */
public class AccountDaoImpl extends BaseDaoImpl<AccountMapper,String,AccountPo> implements AccountDao {

    public AccountDaoImpl(){
        super(AccountMapper.class);
    }

    @Override
    public boolean setAlias(String id, String alias) {
        //todo
        return false;
    }

}

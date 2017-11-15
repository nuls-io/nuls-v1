package io.nuls.db.dao;

import io.nuls.db.entity.AccountPo;

/**
 * Created by Niels on 2017/11/15.
 */
public interface AccountDao extends BaseDao<AccountPo, String> {

    public AccountPo getAccount(String id);

    boolean setAlias(String id, String alias);
}

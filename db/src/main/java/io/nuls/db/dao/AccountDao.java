package io.nuls.db.dao;

import io.nuls.db.entity.AccountPo;

/**
 * Created by Niels on 2017/11/15.
 */
public interface AccountDao extends BaseDao<AccountPo, String> {

    boolean setAlias(String id, String alias);
}

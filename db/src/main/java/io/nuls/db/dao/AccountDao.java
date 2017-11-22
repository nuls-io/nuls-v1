package io.nuls.db.dao;

import io.nuls.db.entity.AccountPo;

/**
 *
 * @author Niels
 * @date 2017/11/15
 */
public interface AccountDao extends BaseDao<String, AccountPo> {

    boolean setAlias(String id, String alias);
}

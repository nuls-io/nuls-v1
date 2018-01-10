package io.nuls.db.dao;

import io.nuls.db.entity.AccountPo;

/**
 *
 * @author Niels
 * @date 2017/11/15
 */
public interface AccountDataService extends BaseDataService<String, AccountPo> {

    AccountPo loadByAddress(String address);

    int updateAlias(AccountPo po);
}

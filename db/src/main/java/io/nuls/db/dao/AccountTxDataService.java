package io.nuls.db.dao;

import io.nuls.core.chain.entity.Result;
import io.nuls.db.entity.AccountPo;

import java.util.List;

/**
 * @author vivi
 * @date 2017/12/22.
 */
public interface AccountTxDataService {

    Result setAlias(String address, String alias);

    void importAccount(List<AccountPo> accountPoList);
}

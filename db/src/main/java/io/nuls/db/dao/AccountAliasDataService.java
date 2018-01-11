package io.nuls.db.dao;

import io.nuls.core.chain.entity.Result;
import io.nuls.db.entity.AccountPo;
import io.nuls.db.entity.AliasPo;

import java.util.List;

/**
 * @author vivi
 * @date 2017/12/22.
 */
public interface AccountAliasDataService {

    Result saveAlias(AliasPo alias);

    void importAccount(List<AccountPo> accountPoList);

    void rollbackAlias(AliasPo aliasPo);
}

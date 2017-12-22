package io.nuls.db.dao.impl.mybatis;

import io.nuls.core.chain.entity.Result;
import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.*;
import io.nuls.db.entity.AccountPo;

import java.util.List;

/**
 * @author vivi
 * @date 2017/12/22.
 */
public class AccountTxDaoImpl implements AccountTxDao {

    private AccountDao accountDao = NulsContext.getInstance().getService(AccountDao.class);

    private AliasDao aliasDao = NulsContext.getInstance().getService(AliasDao.class);

    private TransactionLocalDao txDao = NulsContext.getInstance().getService(TransactionLocalDao.class);

    @Override
    public Result importAccount(List<AccountPo> accountPoList) {
        return null;
    }
}

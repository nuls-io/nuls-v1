package io.nuls.db.dao.impl.mybatis;

import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.db.annotation.TransactionalAnnotation;
import io.nuls.db.dao.AccountDataService;
import io.nuls.db.dao.AccountAliasDataService;
import io.nuls.db.dao.AliasDataService;
import io.nuls.db.dao.TransactionLocalDataService;
import io.nuls.db.entity.AccountPo;
import io.nuls.db.entity.AliasPo;
import io.nuls.db.entity.TransactionLocalPo;

import java.util.List;

/**
 * @author vivi
 * @date 2017/12/22.
 */
public class AccountTxDaoImpl implements AccountAliasDataService {

    private AccountDataService accountDao = NulsContext.getInstance().getService(AccountDataService.class);

    private AliasDataService aliasDao = NulsContext.getInstance().getService(AliasDataService.class);

    private TransactionLocalDataService txDao = NulsContext.getInstance().getService(TransactionLocalDataService.class);

    @TransactionalAnnotation
    @Override
    public Result saveAlias(AliasPo alias) {
        try {
            if (alias.getStatus() == 0) {
                aliasDao.save(alias);
            } else {
                aliasDao.update(alias);
                AccountPo po = new AccountPo();
                po.setId(alias.getAddress());
                po.setAlias(alias.getAlias());
                accountDao.updateAlias(po);
            }
        } catch (Exception e) {
            throw new NulsRuntimeException(ErrorCode.DB_SAVE_ERROR);
        }
        return new Result(true, "OK");
    }

    @TransactionalAnnotation
    @Override
    public void importAccount(List<AccountPo> accountPoList) {
        for (AccountPo account : accountPoList) {
            accountDao.save(account);
            for (int i = 0; i < account.getMyTxs().size(); i++) {
                TransactionLocalPo tx = account.getMyTxs().get(i);
                txDao.save(tx);
            }
        }
    }

    @Override
    public void rollbackAlias(AliasPo aliasPo) {
        try {
            aliasDao.delete(aliasPo.getAlias());

            AccountPo po = new AccountPo();
            po.setId(aliasPo.getAddress());
            po.setAlias("");
            //todo
            accountDao.updateAlias(po);
        } catch (Exception e) {
            throw new NulsRuntimeException(ErrorCode.DB_ROLLBACK_ERROR);
        }
    }
}

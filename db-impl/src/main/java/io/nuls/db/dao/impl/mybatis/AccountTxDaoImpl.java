/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.db.dao.impl.mybatis;

import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.db.dao.AccountDataService;
import io.nuls.db.dao.AccountAliasDataService;
import io.nuls.db.dao.AliasDataService;
import io.nuls.db.dao.TransactionLocalDataService;
import io.nuls.db.entity.AccountPo;
import io.nuls.db.entity.AliasPo;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.transactional.annotation.TransactionalAnnotation;

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
                po.setAddress(alias.getAddress());
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
            po.setAddress(aliasPo.getAddress());
            po.setAlias("");
            accountDao.updateAlias(po);
        } catch (Exception e) {
            throw new NulsRuntimeException(ErrorCode.DB_ROLLBACK_ERROR);
        }
    }
}

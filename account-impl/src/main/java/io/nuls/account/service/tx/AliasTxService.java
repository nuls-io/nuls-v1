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
package io.nuls.account.service.tx;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Alias;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.service.impl.AccountCacheService;
import io.nuls.account.util.AccountTool;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AccountAliasDataService;
import io.nuls.db.entity.AliasPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.service.intf.TransactionService;

import java.util.List;

/**
 * @author Niels
 * @date 2018/1/8
 */
@DbSession(transactional = PROPAGATION.NONE)
public class AliasTxService implements TransactionService<AliasTransaction> {


    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    public AliasTxService() {

    }


    private AccountAliasDataService aliasDataService;

    @Override
    @DbSession
    public void onRollback(AliasTransaction tx, Block block) throws NulsException {
        AliasPo po = AccountTool.toAliasPojo(tx.getTxData());
        getAliasDataService().rollbackAlias(po);
        Account account = accountCacheService.getAccountByAddress(po.getAddress());
        if (account != null) {
            account.setAlias("");
            accountCacheService.putAccount(account);
        }
    }

    @Override
    @DbSession
    public void onCommit(AliasTransaction tx, Block block) throws NulsException {
        Alias alias = tx.getTxData();
        alias.setStatus(1);
        getAliasDataService().saveAlias(AccountTool.toAliasPojo(alias));
        Account account = accountCacheService.getAccountByAddress(alias.getAddress());
        if (account != null) {
            account.setAlias(alias.getAlias());
            accountCacheService.putAccount(account);
        }
    }

    @Override
    public ValidateResult conflictDetect(AliasTransaction tx, List<Transaction> txList) {
        // todo auto-generated method stub(niels)
        return null;
    }

//    @Override
//    @DbSession
//    public void onApproval(AliasTransaction tx, Block block) throws NulsException {
//        Alias alias = tx.getTxData();
//        AliasPo po = getAliasDataService().getAlias(alias.getAlias());
//        if (po != null) {
//            throw new NulsException(ErrorCode.ALIAS_EXIST);
//        }
//    }

    private AccountAliasDataService getAliasDataService() {
        if (aliasDataService == null) {
            aliasDataService = NulsContext.getServiceBean(AccountAliasDataService.class);
        }
        return aliasDataService;
    }
}

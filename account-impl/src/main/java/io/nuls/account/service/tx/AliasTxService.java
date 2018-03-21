/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.account.service.tx;

import io.nuls.account.entity.Alias;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.util.AccountTool;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.db.dao.AccountAliasDataService;
import io.nuls.db.entity.AliasPo;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class AliasTxService implements TransactionService<AliasTransaction> {

    private static AliasTxService instance = new AliasTxService();

    private AliasTxService() {

    }

    public static AliasTxService getInstance() {
        return instance;
    }


    private AccountAliasDataService aliasDataService;

    @Override
    public void onRollback(AliasTransaction tx) throws NulsException {
        AliasPo po = AccountTool.toAliasPojo(tx.getTxData());
        aliasDataService.rollbackAlias(po);
    }

    @Override
    public void onCommit(AliasTransaction tx) throws NulsException {
        Alias alias = tx.getTxData();
        alias.setStatus(1);
        aliasDataService.saveAlias(AccountTool.toAliasPojo(alias));
    }

    @Override
    public void onApproval(AliasTransaction tx) throws NulsException {
        Alias alias = tx.getTxData();
        AliasPo po = aliasDataService.getAlias(alias.getAlias());
        if (alias != null) {
            throw new NulsException(ErrorCode.ALIAS_EXIST);
        }
    }

    public void setDataService(AccountAliasDataService dataService) {
        this.aliasDataService = dataService;
    }
}

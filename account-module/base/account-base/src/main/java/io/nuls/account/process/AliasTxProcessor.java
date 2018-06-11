/*
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
 *
 */

package io.nuls.account.process;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Alias;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.validate.ValidateResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 设置别名处理器
 *
 * @author: Charlie
 * @date: 2018/5/13
 */
@Component
public class AliasTxProcessor implements TransactionProcessor<AliasTransaction> {

    @Autowired
    private AliasService aliasService;

    @Override
    public Result onRollback(AliasTransaction tx, Object secondaryData) {
        Alias alias = tx.getTxData();
        try {
            return aliasService.rollbackAlias(new AliasPo(alias));
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.ALIAS_ROLLBACK_ERROR);
        }
    }

    @Override
    public Result onCommit(AliasTransaction tx, Object secondaryData) {
        Alias alias = tx.getTxData();
        try {
            return aliasService.saveAlias(new AliasPo(alias));
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED);
        }
    }

    /**
     * 冲突检测
     * 1.检测一个acount只能设置一个别名
     * 2.检查是否多个交易设置了同样的别名
     * conflictDetect
     * 1.Detecting an acount can only set one alias.
     * 2.Check if multiple aliasTransaction have the same alias.
     *
     * @param txList 需要检查的交易列表/A list of transactions to be checked.
     * @return
     */
    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        if (null == txList || txList.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        Set<String> aliasNames = new HashSet<>();
        Set<String> accountAddress = new HashSet<>();
        for (Transaction transaction : txList) {
            switch (transaction.getType()) {
                case AccountConstant.TX_TYPE_ACCOUNT_ALIAS:
                    AliasTransaction aliasTransaction = (AliasTransaction) transaction;
                    Alias alias = aliasTransaction.getTxData();
                    if (!aliasNames.add(alias.getAlias())) {
                        return (ValidateResult)ValidateResult.getFailedResult(getClass().getName(), AccountErrorCode.FAILED,
                                "There is another AliasTransaction set the same alias!").setData(aliasTransaction);
                    }
                    if (!accountAddress.add(Hex.encode(alias.getAddress()))) {
                        return (ValidateResult)ValidateResult.getFailedResult(getClass().getName(), AccountErrorCode.FAILED,
                                "An account can only set one alias.!").setData(aliasTransaction);
                    }
                    break;
            }

        }
        return ValidateResult.getSuccessResult();
    }
}

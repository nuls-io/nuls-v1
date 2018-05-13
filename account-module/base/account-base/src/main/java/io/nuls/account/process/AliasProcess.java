package io.nuls.account.process;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Alias;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.core.tools.crypto.Hex;
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
public class AliasProcess implements TransactionProcessor<AliasTransaction> {

    @Autowired
    private AliasService aliasService;

    @Override
    public Result onRollback(AliasTransaction tx, Object secondaryData) {
        Alias alias = tx.getTxData();
        return aliasService.rollbackAlias(new AliasPo(alias));
    }

    @Override
    public Result onCommit(AliasTransaction tx, Object secondaryData) {
        Alias alias = tx.getTxData();
        return aliasService.saveAlias(new AliasPo(alias));
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
                        return ValidateResult.getFailedResult(getClass().getName(), AccountErrorCode.FAILED,
                                "There is another AliasTransaction set the same alias!");
                    }
                    if (!accountAddress.add(Hex.encode(alias.getAddress()))) {
                        return ValidateResult.getFailedResult(getClass().getName(), AccountErrorCode.FAILED,
                                "An account can only set one alias.!");
                    }
                    break;
            }

        }
        return ValidateResult.getSuccessResult();
    }
}

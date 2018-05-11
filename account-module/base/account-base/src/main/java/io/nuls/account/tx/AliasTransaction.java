package io.nuls.account.tx;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.model.Alias;
import io.nuls.kernel.model.Transaction;

/**
 * @author: Charlie
 * @date: 2018/5/11
 */
public class AliasTransaction extends Transaction<Alias> {

    public AliasTransaction() {
        super(AccountConstant.TX_TYPE_ACCOUNT_ALIAS);
    }

    protected AliasTransaction(int type) {
        super(type);
    }

}

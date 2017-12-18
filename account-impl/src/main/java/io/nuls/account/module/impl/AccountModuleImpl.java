package io.nuls.account.module.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.entity.validator.AliasValidator;
import io.nuls.account.manager.AccountManager;
import io.nuls.account.module.intf.AbstractAccountModule;
import io.nuls.account.service.impl.AccountServiceImpl;
import io.nuls.account.service.intf.AccountService;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class AccountModuleImpl extends AbstractAccountModule {

    private AccountManager manager = AccountManager.getInstance();

    @Override

    public void start() {
        manager.init();
        AccountService accountService = AccountServiceImpl.getInstance();
        this.registerService(accountService);
        this.registerTransaction(AccountConstant.TX_TYPE_ALIAS, AliasTransaction.class);
        AliasValidator.getInstance().setAccountService(accountService);
    }

    @Override
    public void shutdown() {
        manager.clearCache();
    }

    @Override
    public void destroy() {
        manager.destroy();
    }

    @Override
    public String getInfo() {
        return "account module is " + this.getStatus();
    }

    @Override
    public int getVersion() {
        return AccountConstant.ACCOUNT_MODULE_VERSION;
    }
}

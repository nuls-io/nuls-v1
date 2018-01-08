package io.nuls.account.module.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.entity.validator.AliasValidator;
import io.nuls.account.manager.AccountManager;
import io.nuls.account.module.intf.AbstractAccountModule;
import io.nuls.account.service.impl.AccountServiceImpl;
import io.nuls.account.service.intf.AccountService;
import io.nuls.account.service.tx.AliasTxService;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.service.intf.EventBusService;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class AccountModuleBootstrap extends AbstractAccountModule {

    private AccountManager manager = AccountManager.getInstance();
    private EventBusService eventBusService = NulsContext.getInstance().getService(EventBusService.class);

    @Override
    public void init() {
        this.registerTransaction(TransactionConstant.TX_TYPE_SET_ALIAS, AliasTransaction.class,new AliasTxService());
    }

    @Override
    public void start() {
        AccountService accountService = AccountServiceImpl.getInstance();
        this.registerService(accountService);
        AliasValidator.getInstance().setAccountService(accountService);
        manager.init();
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

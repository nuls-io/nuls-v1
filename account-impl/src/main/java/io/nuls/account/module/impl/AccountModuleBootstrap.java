package io.nuls.account.module.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.entity.validator.AliasValidator;
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

    private EventBusService eventBusService;
    private AccountService accountService;

    @Override
    public void init() {
        accountService = AccountServiceImpl.getInstance();
        accountService.init();
        this.registerService(accountService);
        eventBusService = NulsContext.getInstance().getService(EventBusService.class);

        this.registerTransaction(TransactionConstant.TX_TYPE_SET_ALIAS, AliasTransaction.class,new AliasTxService());
    }

    @Override
    public void start() {
        accountService.start();
    }

    @Override
    public void shutdown() {
        accountService.shutdown();
    }

    @Override
    public void destroy() {
        accountService.destroy();
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

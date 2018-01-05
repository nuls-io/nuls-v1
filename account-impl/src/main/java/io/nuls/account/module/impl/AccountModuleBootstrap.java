package io.nuls.account.module.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.event.AliasEvent;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.entity.validator.AliasValidator;
import io.nuls.account.event.filter.AliasEventFilter;
import io.nuls.account.event.handler.AliasEventHandler;
import io.nuls.account.manager.AccountManager;
import io.nuls.account.module.intf.AbstractAccountModule;
import io.nuls.account.service.impl.AccountServiceImpl;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.network.service.NetworkService;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class AccountModuleBootstrap extends AbstractAccountModule {

    private AccountManager manager = AccountManager.getInstance();
    private EventBusService eventBusService = NulsContext.getInstance().getService(EventBusService.class);

    @Override
    public void init() {
        this.registerTransaction(TransactionConstant.TX_TYPE_SET_ALIAS, AliasTransaction.class);
    }

    @Override
    public void start() {
        AccountService accountService = AccountServiceImpl.getInstance();
        this.registerService(accountService);
        AliasValidator.getInstance().setAccountService(accountService);
        manager.init();
        registerHandlers();
    }

    private void registerHandlers() {
        LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
        AliasEventHandler.getInstance().addFilter(AliasEventFilter.getInstance());
        AliasEventHandler.getInstance().setLedgerService(ledgerService);
        eventBusService.subscribeNetworkEvent(AliasEvent.class, AliasEventHandler.getInstance());
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

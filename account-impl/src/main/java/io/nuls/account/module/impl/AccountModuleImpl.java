package io.nuls.account.module.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.event.AliasEvent;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.entity.validator.AliasValidator;
import io.nuls.account.event.filter.AliasFilter;
import io.nuls.account.event.handler.AliasHandler;
import io.nuls.account.manager.AccountManager;
import io.nuls.account.module.intf.AbstractAccountModule;
import io.nuls.account.service.impl.AccountServiceImpl;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.processor.service.intf.EventProcessorService;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.network.service.NetworkService;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class AccountModuleImpl extends AbstractAccountModule {

    private AccountManager manager = AccountManager.getInstance();
    private EventProcessorService processorService = NulsContext.getInstance().getService(EventProcessorService.class);

    @Override
    public void start() {
        AccountService accountService = AccountServiceImpl.getInstance();
        this.registerService(accountService);
        this.registerTransaction(TransactionConstant.TX_TYPE_SET_ALIAS, AliasTransaction.class);
        this.registerBusDataClass(AccountConstant.EVENT_TYPE_ALIAS, AliasEvent.class);
        AliasValidator.getInstance().setAccountService(accountService);
        manager.init();
        registerHanders();
    }

    private void registerHanders() {
        NetworkService service = NulsContext.getInstance().getService(NetworkService.class);
        LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

        AliasHandler.getInstance().addFilter(AliasFilter.getInstance());
        AliasHandler.getInstance().setLedgerService(ledgerService);
        processorService.registerEventHandler(AliasEvent.class, AliasHandler.getInstance());
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

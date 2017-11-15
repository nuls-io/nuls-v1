package io.nuls.ledger.module.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.event.LockEvent;
import io.nuls.ledger.event.SmallChangeEvent;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.ledger.handler.LockHandler;
import io.nuls.ledger.handler.SmallChangeHandler;
import io.nuls.ledger.handler.TransactionHandler;
import io.nuls.ledger.module.LedgerModule;
import io.nuls.ledger.service.impl.LedgerServiceImpl;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.thread.SmallChangeThread;

import java.util.List;

/**
 * Created by Niels on 2017/11/7.
 */
public class LedgerModuleImpl extends LedgerModule {

    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private CacheService<String,Balance> cacheService = NulsContext.getInstance().getService(CacheService.class);

    private LedgerService ledgerService = LedgerServiceImpl.getInstance();

    private NetworkProcessorService processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);

    @Override
    public void start() {
        cacheStandingBook();
        this.registerService(ledgerService);
        SmallChangeThread.getInstance().start();
        //register handler
        this.processorService.registerEventHandler(LockEvent.class, new LockHandler());
        this.processorService.registerEventHandler(SmallChangeEvent.class, new SmallChangeHandler());
        this.processorService.registerEventHandler(TransactionEvent.class, new TransactionHandler());
    }

    private void cacheStandingBook() {
        cacheService.createCache(LedgerConstant.STANDING_BOOK);
        //load account
        List<Account> accounts = this.accountService.getLocalAccountList();
        if (null == accounts) {
            return;
        }
        //calc balance
        //put standing book to cache
        for (Account account : accounts) {
            Balance balance = this.ledgerService.getBalance(account.getAddress().toString());
            this.cacheService.putElement(LedgerConstant.STANDING_BOOK, account.getAddress().toString(), balance);
        }
    }

    @Override
    public void shutdown() {
        this.cacheService.removeCache(LedgerConstant.STANDING_BOOK);
        SmallChangeThread.getInstance().stop();
    }

    @Override
    public void destroy() {
        this.cacheService.removeCache(LedgerConstant.STANDING_BOOK);
        SmallChangeThread.getInstance().stop();
    }

    @Override
    public String getInfo() {
        //todo
        return null;
    }

    @Override
    public int getVersion() {
        return LedgerConstant.LEDGER_MODULE_VERSION;
    }
}

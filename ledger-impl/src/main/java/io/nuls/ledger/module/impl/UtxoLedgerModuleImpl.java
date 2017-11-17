package io.nuls.ledger.module.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.event.CoinTransactionEvent;
import io.nuls.ledger.event.UtxoLockEvent;
import io.nuls.ledger.event.UtxoSmallChangeEvent;
import io.nuls.ledger.handler.UtxoCoinTransactionHandler;
import io.nuls.ledger.handler.UtxoLockHandler;
import io.nuls.ledger.handler.UtxoSmallChangeHandler;
import io.nuls.ledger.module.LedgerModule;
import io.nuls.ledger.service.impl.UtxoLedgerServiceImpl;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.thread.SmallChangeThread;

import java.util.List;

/**
 * Created by Niels on 2017/11/7.
 */
public class UtxoLedgerModuleImpl extends LedgerModule {

    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private CacheService<String,Balance> cacheService = NulsContext.getInstance().getService(CacheService.class);

    private LedgerService ledgerService = UtxoLedgerServiceImpl.getInstance();

    private NetworkProcessorService processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);

    @Override
    public void start() {
        cacheStandingBook();
        this.registerService(ledgerService);
        SmallChangeThread.getInstance().start();
        //register handler
//        this.registerEvent((short)1, BaseLedgerEvent.class);
        this.registerEvent((short)2, UtxoLockEvent.class);
        this.registerEvent((short)3, UtxoSmallChangeEvent.class);
        this.registerEvent((short)4, CoinTransactionEvent.class);
        this.processorService.registerEventHandler(UtxoLockEvent.class, new UtxoLockHandler());
        this.processorService.registerEventHandler(UtxoSmallChangeEvent.class, new UtxoSmallChangeHandler());
        this.processorService.registerEventHandler(CoinTransactionEvent.class, new UtxoCoinTransactionHandler());
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
            this.ledgerService.getBalance(account.getAddress().toString());
        }
    }

    @Override
    public void shutdown() {
        cacheService.clear();
        SmallChangeThread.getInstance().stop();
    }

    @Override
    public void destroy() {
        this.cacheService.destroy();
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

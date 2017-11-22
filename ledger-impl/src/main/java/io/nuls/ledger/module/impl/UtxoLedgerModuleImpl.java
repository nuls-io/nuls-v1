package io.nuls.ledger.module.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.event.AbstractCoinTransactionEvent;
import io.nuls.ledger.event.UtxoDepositCoinEvent;
import io.nuls.ledger.event.UtxoLockCoinEvent;
import io.nuls.ledger.event.UtxoSmallChangeEvent;
import io.nuls.ledger.handler.UtxoCoinTransactionHandler;
import io.nuls.ledger.handler.UtxoLockHandler;
import io.nuls.ledger.handler.UtxoSmallChangeHandler;
import io.nuls.ledger.module.AbstractLedgerModule;
import io.nuls.ledger.service.impl.LedgerCacheService;
import io.nuls.ledger.service.impl.UtxoLedgerServiceImpl;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.thread.SmallChangeThread;

import java.util.List;

/**
 * Created by Niels on 2017/11/7.
 */
public class UtxoLedgerModuleImpl extends AbstractLedgerModule {

    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private LedgerCacheService cacheService = LedgerCacheService.getInstance();

    private LedgerService ledgerService = UtxoLedgerServiceImpl.getInstance();

    private NetworkProcessorService processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);

    @Override
    public void start() {
        cacheStandingBook();
        this.registerService(ledgerService);
        SmallChangeThread.getInstance().start();
        //register handler
//        this.registerEvent((short) 3, BaseUtxoCoinEvent.class);
        this.registerEvent((short) 4, UtxoLockCoinEvent.class);
        this.registerEvent((short) 5, UtxoSmallChangeEvent.class);
        this.registerEvent((short) 6, AbstractCoinTransactionEvent.class);
        this.registerEvent((short) 7, UtxoDepositCoinEvent.class);
        this.processorService.registerEventHandler(UtxoLockCoinEvent.class, new UtxoLockHandler());
        this.processorService.registerEventHandler(UtxoSmallChangeEvent.class, new UtxoSmallChangeHandler());
        this.processorService.registerEventHandler(AbstractCoinTransactionEvent.class, new UtxoCoinTransactionHandler());
    }

    private void cacheStandingBook() {
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

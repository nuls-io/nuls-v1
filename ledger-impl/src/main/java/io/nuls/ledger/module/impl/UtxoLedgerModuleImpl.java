package io.nuls.ledger.module.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.validator.CommonTxValidatorManager;
import io.nuls.ledger.event.AbstractCoinTransactionEvent;
import io.nuls.ledger.event.UtxoDepositNulsEvent;
import io.nuls.ledger.event.UtxoLockNulsEvent;
import io.nuls.ledger.event.UtxoSmallChangeEvent;
import io.nuls.ledger.handler.UtxoCoinTransactionHandler;
import io.nuls.ledger.handler.UtxoLockHandler;
import io.nuls.ledger.handler.UtxoSmallChangeHandler;
import io.nuls.ledger.module.AbstractLedgerModule;
import io.nuls.ledger.service.impl.LedgerCacheServiceImpl;
import io.nuls.ledger.service.impl.UtxoLedgerServiceImpl;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.thread.SmallChangeThread;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class UtxoLedgerModuleImpl extends AbstractLedgerModule {

    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private LedgerCacheServiceImpl cacheService = LedgerCacheServiceImpl.getInstance();

    private LedgerService ledgerService = UtxoLedgerServiceImpl.getInstance();

    private NetworkProcessorService processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);

    @Override
    public void start() {
        CommonTxValidatorManager.initTxValidators();
        cacheStandingBook();
        this.registerService(ledgerService);
        SmallChangeThread smallChangeThread = SmallChangeThread.getInstance();
        ThreadManager.createSingleThreadAndRun(this.getModuleId(), SmallChangeThread.class.getSimpleName(), smallChangeThread);
        this.registerEvent((short) 4, UtxoLockNulsEvent.class);
        this.registerEvent((short) 5, UtxoSmallChangeEvent.class);
        this.registerEvent((short) 6, AbstractCoinTransactionEvent.class);
        this.registerEvent((short) 7, UtxoDepositNulsEvent.class);
        this.processorService.registerEventHandler(UtxoLockNulsEvent.class, new UtxoLockHandler());
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
    }

    @Override
    public void destroy() {
        this.cacheService.destroy();
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

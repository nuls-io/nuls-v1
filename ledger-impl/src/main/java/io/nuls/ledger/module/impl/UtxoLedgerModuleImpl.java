package io.nuls.ledger.module.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.event.bus.processor.service.intf.EventProcessorService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.validator.CommonTxValidatorManager;
import io.nuls.ledger.event.AbstractCoinTransactionEvent;
import io.nuls.ledger.event.UtxoLockNulsEvent;
import io.nuls.ledger.event.UtxoSmallChangeEvent;
import io.nuls.ledger.handler.UtxoCoinTransactionBusHandler;
import io.nuls.ledger.handler.UtxoLockBusHandler;
import io.nuls.ledger.handler.UtxoSmallChangeBusHandler;
import io.nuls.ledger.module.AbstractLedgerModule;
import io.nuls.ledger.service.impl.LedgerCacheServiceImpl;
import io.nuls.ledger.service.impl.UtxoCoinDataProvider;
import io.nuls.ledger.service.impl.UtxoLedgerServiceImpl;
import io.nuls.ledger.service.intf.CoinDataProvider;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.thread.SmallChangeThread;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class UtxoLedgerModuleImpl extends AbstractLedgerModule {


    private LedgerCacheServiceImpl cacheService = LedgerCacheServiceImpl.getInstance();

    private LedgerService ledgerService = UtxoLedgerServiceImpl.getInstance();

    private EventProcessorService processorService = NulsContext.getInstance().getService(EventProcessorService.class);

    @Override
    public void init() {
        CommonTxValidatorManager.initTxValidators();
    }

    @Override
    public void start() {
        this.registerService(ledgerService);
        this.registerService(CoinDataProvider.class, UtxoCoinDataProvider.getInstance());
        cacheStandingBook();
        SmallChangeThread smallChangeThread = SmallChangeThread.getInstance();
        ThreadManager.createSingleThreadAndRun(this.getModuleId(), SmallChangeThread.class.getSimpleName(), smallChangeThread);
        this.publish((short) 4, UtxoLockNulsEvent.class);
        this.publish((short) 5, UtxoSmallChangeEvent.class);
        this.publish((short) 6, AbstractCoinTransactionEvent.class);
        this.processorService.registerEventHandler(UtxoLockNulsEvent.class, new UtxoLockBusHandler());
        this.processorService.registerEventHandler(UtxoSmallChangeEvent.class, new UtxoSmallChangeBusHandler());
        this.processorService.registerEventHandler(AbstractCoinTransactionEvent.class, new UtxoCoinTransactionBusHandler());
    }

    private void cacheStandingBook() {
        //load account
        List<Account> accounts = NulsContext.getInstance().getService(AccountService.class).getLocalAccountList();
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

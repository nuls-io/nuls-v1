package io.nuls.ledger.module.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.manager.TransactionValidatorManager;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.module.AbstractLedgerModule;
import io.nuls.ledger.service.impl.LedgerCacheService;
import io.nuls.ledger.service.impl.UtxoCoinDataProvider;
import io.nuls.ledger.service.impl.UtxoLedgerServiceImpl;
import io.nuls.ledger.service.intf.CoinDataProvider;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.thread.SmallChangeThread;
import io.nuls.ledger.validator.TxFieldValidator;
import io.nuls.ledger.validator.TxMaxSizeValidator;
import io.nuls.ledger.validator.TxRemarkValidator;
import io.nuls.ledger.validator.TxSignValidator;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class UtxoLedgerModuleBootstrap extends AbstractLedgerModule {

    private LedgerCacheService cacheService;

    private LedgerService ledgerService;

    private EventBusService eventBusService;

    @Override
    public void init() {
        cacheService = LedgerCacheService.getInstance();
        ledgerService = UtxoLedgerServiceImpl.getInstance();
        eventBusService = NulsContext.getInstance().getService(EventBusService.class);

        addNormalTxValidator();
        registerService();
    }

    /**
     * there validators any kind of transaction will be used
     */
    private void addNormalTxValidator() {
        TransactionValidatorManager.addTxDefValidator(TxMaxSizeValidator.getInstance());
        TransactionValidatorManager.addTxDefValidator(TxRemarkValidator.getInstance());
        TransactionValidatorManager.addTxDefValidator(TxFieldValidator.getInstance());
        TransactionValidatorManager.addTxDefValidator(TxSignValidator.getInstance());
    }

    private void registerService() {
        this.registerService(ledgerService);
        this.registerService(CoinDataProvider.class, UtxoCoinDataProvider.getInstance());
    }

    @Override
    public void start() {
        cacheAccountAndBalance();

        SmallChangeThread smallChangeThread = SmallChangeThread.getInstance();
        TaskManager.createAndRunThread(this.getModuleId(), SmallChangeThread.class.getSimpleName(), smallChangeThread);
    }

    private void cacheAccountAndBalance() {
        //load account
        List<Account> accounts = NulsContext.getInstance().getService(AccountService.class).getAccountList();
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

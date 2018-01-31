/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.ledger.module.impl;

import io.nuls.core.chain.manager.TransactionValidatorManager;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.db.dao.UtxoInputDataService;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.listener.CoinDataTxService;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.TransferTransaction;
import io.nuls.ledger.entity.tx.UnlockNulsTransaction;
import io.nuls.ledger.module.AbstractLedgerModule;
import io.nuls.ledger.service.impl.LedgerCacheService;
import io.nuls.ledger.service.impl.UtxoCoinDataProvider;
import io.nuls.ledger.service.impl.UtxoCoinManager;
import io.nuls.ledger.service.impl.UtxoLedgerServiceImpl;
import io.nuls.ledger.service.intf.CoinDataProvider;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.thread.SmallChangeThread;
import io.nuls.ledger.validator.TxFieldValidator;
import io.nuls.ledger.validator.TxMaxSizeValidator;
import io.nuls.ledger.validator.TxRemarkValidator;
import io.nuls.ledger.validator.TxSignValidator;


/**
 * @author Niels
 * @date 2017/11/7
 */
public class UtxoLedgerModuleBootstrap extends AbstractLedgerModule {

    private LedgerCacheService cacheService;

    private LedgerService ledgerService;

    private UtxoCoinManager coinManager;

    @Override
    public void init() {
        cacheService = LedgerCacheService.getInstance();
        registerService();
        ledgerService = NulsContext.getServiceBean(LedgerService.class);
        coinManager = UtxoCoinManager.getInstance();
        UtxoOutputDataService outputDataService = NulsContext.getServiceBean(UtxoOutputDataService.class);
        coinManager.setOutputDataService(outputDataService);
        addNormalTxValidator();
        this.registerTransaction(TransactionConstant.TX_TYPE_COIN_BASE, CoinBaseTransaction.class, CoinDataTxService.getInstance());
        this.registerTransaction(TransactionConstant.TX_TYPE_TRANSFER, TransferTransaction.class, CoinDataTxService.getInstance());
        this.registerTransaction(TransactionConstant.TX_TYPE_UNLOCK, UnlockNulsTransaction.class, CoinDataTxService.getInstance());
        this.registerTransaction(TransactionConstant.TX_TYPE_LOCK, LockNulsTransaction.class, CoinDataTxService.getInstance());
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
        this.registerService(UtxoLedgerServiceImpl.class);
        this.registerService(UtxoCoinDataProvider.class);
    }

    @Override
    public void start() {
        ledgerService.init();
        coinManager.cacheAllUnSpendOutPut();

        SmallChangeThread smallChangeThread = SmallChangeThread.getInstance();
        TaskManager.createAndRunThread(this.getModuleId(), SmallChangeThread.class.getSimpleName(), smallChangeThread);
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

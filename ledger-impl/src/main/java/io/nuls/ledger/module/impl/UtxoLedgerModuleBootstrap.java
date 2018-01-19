/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
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
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.ledger.constant.LedgerConstant;
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
        ledgerService = UtxoLedgerServiceImpl.getInstance();
        coinManager = UtxoCoinManager.getInstance();
        UtxoOutputDataService outputDataService = NulsContext.getInstance().getService(UtxoOutputDataService.class);
        coinManager.setOutputDataService(outputDataService);
        ledgerService.init();
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

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

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.TxAccountRelationPo;
import io.nuls.ledger.entity.listener.CoinDataTxService;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.TransferTransaction;
import io.nuls.ledger.entity.tx.UnlockNulsTransaction;
import io.nuls.ledger.entity.validator.CoinTransactionValidatorManager;
import io.nuls.ledger.event.notice.BalanceChangeNotice;
import io.nuls.ledger.module.AbstractLedgerModule;
import io.nuls.ledger.service.impl.LedgerCacheService;
import io.nuls.ledger.service.impl.UtxoCoinDataProvider;
import io.nuls.ledger.service.impl.UtxoCoinManager;
import io.nuls.ledger.service.impl.UtxoLedgerServiceImpl;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.thread.ReSendTxThread;
import io.nuls.ledger.thread.SmallChangeThread;
import io.nuls.ledger.util.UtxoTransactionTool;
import io.nuls.ledger.validator.TxFieldValidator;
import io.nuls.ledger.validator.TxSignValidator;
import io.nuls.ledger.validator.UtxoTxInputsValidator;
import io.nuls.ledger.validator.UtxoTxOutputsValidator;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.manager.EventManager;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.service.intf.TransactionService;
import io.nuls.protocol.utils.TransactionManager;
import io.nuls.protocol.utils.TransactionValidatorManager;

import java.util.List;


/**
 * @author Niels
 * @date 2017/11/7
 */
public class UtxoLedgerModuleBootstrap extends AbstractLedgerModule {

    private LedgerService ledgerService;

    private UtxoCoinManager coinManager;

    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();

    @Override
    public void init() {
        this.waitForDependencyInited(NulsConstant.MODULE_ID_DB);
        EventManager.putEvent(BalanceChangeNotice.class);
        registerService();
        ledgerService = NulsContext.getServiceBean(LedgerService.class);
        coinManager = UtxoCoinManager.getInstance();
        addNormalTxValidator();
    }

    /**
     * there validators any kind of transaction will be used
     */
    private void addNormalTxValidator() {
        TransactionValidatorManager.addTxDefValidator(TxFieldValidator.getInstance());
        TransactionValidatorManager.addTxDefValidator(TxSignValidator.getInstance());

        CoinTransactionValidatorManager.addTxDefValidator(UtxoTxInputsValidator.getInstance());
        CoinTransactionValidatorManager.addTxDefValidator(UtxoTxOutputsValidator.getInstance());
    }

    private void registerService() {
        this.registerTransaction(TransactionConstant.TX_TYPE_COIN_BASE, CoinBaseTransaction.class, CoinDataTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_TRANSFER, TransferTransaction.class, CoinDataTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_UNLOCK, UnlockNulsTransaction.class, CoinDataTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_LOCK, LockNulsTransaction.class, CoinDataTxService.class);

        this.registerService(UtxoLedgerServiceImpl.class);
        this.registerService(UtxoCoinDataProvider.class);
    }

    protected final void registerTransaction(int txType, Class<? extends Transaction> txClass, Class<? extends TransactionService> txServiceClass) {
        this.registerService(txServiceClass);
        TransactionManager.putTx(txType, txClass, txServiceClass);
    }

    @Override
    public void start() {
        //cache the wallet's all accounts unSpend output
        coinManager.cacheAllUnSpendUtxo();
        try {
            List<Transaction> localTxList = ledgerService.getLocalUnConfirmTxList();
            for(Transaction tx : localTxList) {
                ledgerCacheService.putLocalTx(tx);
            }
        } catch (NulsException e) {
            Log.error(e);
        }
        TaskManager.createAndRunThread(this.getModuleId(), SmallChangeThread.class.getSimpleName(), SmallChangeThread.getInstance());
        TaskManager.createAndRunThread(this.getModuleId(), ReSendTxThread.class.getSimpleName(), ReSendTxThread.getInstance());
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getInfo() {
        //todo
        return null;
    }

}

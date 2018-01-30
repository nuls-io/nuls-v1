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
package io.nuls.ledger.service.impl;

import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.db.dao.UtxoTransactionDataService;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.db.transactional.annotation.TransactionalAnnotation;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.UtxoBalance;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.TransferTransaction;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.util.UtxoTransactionTool;
import io.nuls.ledger.util.UtxoTransferTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class UtxoLedgerServiceImpl implements LedgerService {

    private static final LedgerService INSTANCE = new UtxoLedgerServiceImpl();

    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();

    private UtxoTransactionDataService txDao;

    private EventBroadcaster eventBroadcaster;

    private Lock lock = new ReentrantLock();

    private UtxoLedgerServiceImpl() {

    }

    public static LedgerService getInstance() {
        return INSTANCE;
    }

    @Override
    public void init() {
        txDao = NulsContext.getInstance().getService(UtxoTransactionDataService.class);
        eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        TransactionPo po = txDao.gettx(hash.getDigestHex());
        try {
            return UtxoTransferTool.toTransaction(po);
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }

    @Override
    public Transaction getLocalTx(NulsDigestData hash) {
        TransactionLocalPo po = txDao.getLocaltx(hash.getDigestHex());
        try {
            return UtxoTransferTool.toTransaction(po);
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }

    @Override
    public List<Transaction> getTxList(String address, int txType, int start, int limit) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        boolean isLocal = NulsContext.LOCAL_ADDRESS_LIST.contains(address);
        if (isLocal) {
            List<TransactionLocalPo> poList = txDao.getLocalTxs(address, txType, start, limit);
            for (TransactionLocalPo po : poList) {
                txList.add(UtxoTransferTool.toTransaction(po));
            }
        } else {
            List<TransactionPo> poList = txDao.getTxs(address, txType, start, limit);
            for (TransactionPo po : poList) {
                txList.add(UtxoTransferTool.toTransaction(po));
            }
        }
        return txList;
    }

    @Override
    public List<Transaction> getTxList(String address, int txType) throws Exception {
        return getTxList(address, txType, 0, 0);
    }

    @Override
    public List<Transaction> getTxList(String blockHash) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        List<TransactionPo> poList = txDao.getTxs(blockHash);
        for (TransactionPo po : poList) {
            txList.add(UtxoTransferTool.toTransaction(po));
        }
        return txList;
    }

    @Override
    public List<Transaction> getTxList(long startHeight, long endHeight) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        List<TransactionPo> poList = txDao.getTxs(startHeight, endHeight);
        for (TransactionPo po : poList) {
            txList.add(UtxoTransferTool.toTransaction(po));
        }
        return txList;
    }

    @Override
    public List<Transaction> getTxList(long height) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        List<TransactionPo> poList = txDao.getTxs(height);
        for (TransactionPo po : poList) {
            txList.add(UtxoTransferTool.toTransaction(po));
        }
        return txList;
    }

    @Override
    public Balance getBalance(String address) {
        Balance balance = ledgerCacheService.getBalance(address);
        if (null == balance) {
            balance = calcBalance(address);
            ledgerCacheService.putBalance(address, balance);
        }
        return balance;
    }

    private Balance calcBalance(String address) {
        UtxoBalance balance = new UtxoBalance();
        List<UtxoOutputPo> unSpendList = txDao.getAccountOutputs(address, TransactionConstant.TX_OUTPUT_UNSPEND);
        if (unSpendList == null || unSpendList.isEmpty()) {
            return null;
        }
        List<UtxoOutput> unSpends = new ArrayList<>();

        long useable = 0;
        long locked = 0;
        for (UtxoOutputPo po : unSpendList) {
            UtxoOutput output = UtxoTransferTool.toOutput(po);
            if (output.getStatus() == 0) {
                useable += output.getValue();
            } else {
                locked += output.getValue();
            }
            unSpends.add(output);
        }
        balance.setUseable(Na.valueOf(useable));
        balance.setLocked(Na.valueOf(locked));
        balance.setBalance(balance.getLocked().add(balance.getUseable()));
        balance.setUnSpends(unSpends);
        return balance;
    }

    @Override
    public Result transfer(String address, String password, String toAddress, Na amount, String remark) {
        CoinTransferData coinData = new CoinTransferData(amount, address, toAddress);
        return transfer(coinData, password, remark);
    }

    private Result transfer(CoinTransferData coinData, String password, String remark) {
        TransferTransaction tx = null;
        try {
            tx = UtxoTransactionTool.getInstance().createTransferTx(coinData, password, remark);
            tx.verify();
            TransactionEvent event = new TransactionEvent();
            event.setEventBody(tx);
            eventBroadcaster.broadcastAndCacheAysn(event, true);
        } catch (Exception e) {
            Log.error(e);
            try {
                rollbackTx(tx);
            } catch (NulsException e1) {
                Log.error(e1);
            }
            return new Result(false, e.getMessage());
        }
        return new Result(true, "OK", tx.getHash().getDigestHex());
    }

    @Override
    public Result transfer(List<String> addressList, String password, String toAddress, Na amount, String remark) {
        CoinTransferData coinData = new CoinTransferData(amount, addressList, toAddress);
        return transfer(coinData, password, remark);
    }


    @Override
    public Result lock(String address, String password, Na amount, long unlockTime, String remark) {
        LockNulsTransaction tx = null;
        try {
            CoinTransferData coinData = new CoinTransferData(amount, address);
            coinData.addTo(address, new Coin(amount, unlockTime));
            tx = UtxoTransactionTool.getInstance().createLockNulsTx(coinData, password, remark);

            tx.verify();
            TransactionEvent event = new TransactionEvent();
            event.setEventBody(tx);
            eventBroadcaster.broadcastAndCacheAysn(event, true);

        } catch (Exception e) {
            Log.error(e);
            try {
                rollbackTx(tx);
            } catch (NulsException e1) {
                Log.error(e1);
            }
            return new Result(false, e.getMessage());
        }

        return new Result(true, "OK", tx.getHash().getDigestHex());
    }

    @Override
    @TransactionalAnnotation
    public boolean saveTxList(List<Transaction> txList) throws IOException {
        lock.lock();
        try {
            List<TransactionPo> poList = new ArrayList<>();
            List<TransactionLocalPo> localPoList = new ArrayList<>();
            for (int i = 0; i < txList.size(); i++) {
                Transaction tx = txList.get(i);
                TransactionPo po = UtxoTransferTool.toTransactionPojo(tx);
                poList.add(po);
                if (tx.isLocalTx()) {
                    TransactionLocalPo localPo = UtxoTransferTool.toLocalTransactionPojo(tx);
                    localPoList.add(localPo);
                }
            }
            txDao.saveTxList(poList);
            if (localPoList.size() > 0) {
                txDao.saveLocalList(localPoList);
            }
        } finally {
            lock.unlock();
        }
        return false;
    }


    @Override
    public boolean checkTxIsMine(Transaction tx) throws NulsException {
        if (tx instanceof AbstractCoinTransaction) {
            return UtxoTransactionTool.getInstance().isMine((AbstractCoinTransaction) tx);
        }
        return false;
    }

    @Override
    public void rollbackTx(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onRollback(tx);
        }
        tx.setStatus(TxStatusEnum.CACHED);
    }

    @Override
    public void commitTx(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onCommit(tx);
        }
        tx.setStatus(TxStatusEnum.CONFIRMED);
    }

    @Override
    public void approvalTx(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onApproval(tx);
        }
        tx.setStatus(TxStatusEnum.AGREED);
    }

    @Override
    public void deleteTx(Transaction tx) {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void deleteTx(long blockHeight) {
        // todo auto-generated method stub(niels)

    }

    public List<TransactionService> getServiceList(Class<? extends Transaction> txClass) {
        List<TransactionService> list = new ArrayList<>();
        Class clazz = txClass;
        while (!clazz.equals(Transaction.class)) {
            TransactionService txService = TransactionManager.getService(clazz);
            if (null != txService) {
                list.add(0, txService);
            }
            clazz = clazz.getSuperclass();
        }
        return list;
    }
}

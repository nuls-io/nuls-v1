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

import io.nuls.account.entity.Account;
import io.nuls.cache.service.intf.CacheService;
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
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.UtxoTransactionDataService;
import io.nuls.db.entity.*;
import io.nuls.db.transactional.annotation.DbSession;
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


    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();
    @Autowired
    private UtxoTransactionDataService txDao;
    @Autowired
    private EventBroadcaster eventBroadcaster;

    private static final String RECEIVE_TX_CACHE = "Received-tx-cache";

    private static final String CONFIRM_TX_CACHE = "Confirming-tx-cache";
    @Autowired
    private CacheService<String, Transaction> txCacheService;

    private Lock lock = new ReentrantLock();

    @Override
    public void init() {
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
    public List<Transaction> getTxList(String address, int txType) throws Exception {
        return getTxList(address, txType, null, null);
    }

    @Override
    public long getTxCount(String address, int txType) throws Exception {
        boolean isLocal = NulsContext.LOCAL_ADDRESS_LIST.contains(address);
        if (isLocal) {
            return txDao.getLocalTxsCount(address, txType);
        } else {
            return txDao.getTxsCount(address, txType);
        }
    }

    @Override
    public List<Transaction> getTxList(String address, int txType, Integer pageNumber, Integer pageSize) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        if (StringUtils.isNotBlank(address) && NulsContext.LOCAL_ADDRESS_LIST.contains(address)) {
            txList = getLocalTxList(address, txType, pageNumber, pageSize);
        } else {
            List<TransactionPo> poList = txDao.getTxs(address, txType, pageNumber, pageSize);
            for (TransactionPo po : poList) {
                txList.add(UtxoTransferTool.toTransaction(po));
            }
        }
        return txList;
    }

    public List<Transaction> getLocalTxList(String address, int txType, Integer pageNumber, Integer pageSize) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        List<Transaction> cacheTxList = txCacheService.getElementList(RECEIVE_TX_CACHE);
        for (Transaction tx : cacheTxList) {
            if (tx.isLocalTx()) {
                txList.add(tx);
            }
        }
        cacheTxList = txCacheService.getElementList(CONFIRM_TX_CACHE);
        for (Transaction tx : cacheTxList) {
            if (tx.isLocalTx()) {
                txList.add(tx);
            }
        }

        List<TransactionLocalPo> poList;
        if (pageNumber == null && pageSize == null) {
            poList = txDao.getLocalTxs(address, txType, null, null);
            for (TransactionLocalPo po : poList) {
                txList.add(UtxoTransferTool.toTransaction(po));
            }
            return txList;
        }

        int start = (pageNumber - 1) * pageSize;
        if (txList.size() >= start + pageSize) {
            return txList.subList(start, start + pageSize);
        }

        start = start - txList.size();
        poList = txDao.getLocalTxs(address, txType, start, pageSize);
        txList = new ArrayList<>();
        for (TransactionLocalPo po : poList) {
            txList.add(UtxoTransferTool.toTransaction(po));
        }
        return txList;
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
        }
        return balance;
    }

    private Balance calcBalance(String address) {

        UtxoBalance balance = new UtxoBalance();
        List<UtxoOutputPo> unSpendList = txDao.getAccountUnSpend(address);
        if (unSpendList == null || unSpendList.isEmpty()) {
            return balance;
        }
        List<UtxoOutput> unSpends = new ArrayList<>();

        for (UtxoOutputPo po : unSpendList) {
            UtxoOutput output = UtxoTransferTool.toOutput(po);
            unSpends.add(output);
        }
        ledgerCacheService.putBalance(address, balance);
        UtxoTransactionTool.getInstance().calcBalance(address);

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
    @DbSession
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
    @DbSession
    public void saveTxInLocal(String address) {
        List<TransactionPo> poList = txDao.getTxs(address, 0, null, null);
        if (poList.isEmpty()) {
            return;
        }
        List<TransactionLocalPo> localPoList = new ArrayList<>();

        for (TransactionPo po : poList) {
            TransactionLocalPo localPo = new TransactionLocalPo(po);
            for (UtxoInputPo inputPo : po.getInputs()) {
                if (inputPo.getFromOutPut().getAddress().equals(address)) {
                    localPo.setTransferType(Transaction.TRANSFER_SEND);
                    break;
                }
            }
            localPoList.add(localPo);
        }
        txDao.saveLocalList(localPoList);
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
        if (tx.getStatus() == TxStatusEnum.CACHED) {
            return;
        }
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onRollback(tx);
        }
        tx.setStatus(TxStatusEnum.CACHED);
    }

    @Override
    public void commitTx(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        if (tx.getStatus() == TxStatusEnum.AGREED) {
            return;
        }
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onCommit(tx);
        }
        tx.setStatus(TxStatusEnum.CONFIRMED);
    }

    @Override
    public void approvalTx(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        if (tx.getStatus() == TxStatusEnum.AGREED || tx.getStatus() == TxStatusEnum.CONFIRMED) {
            return;
        }
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onApproval(tx);
        }
        tx.setStatus(TxStatusEnum.AGREED);
    }

    @Override
    public void deleteTx(Transaction tx) {
        txDao.deleteTx(tx.getHash().getDigestHex());
    }

    @Override
    @DbSession
    public void deleteTx(long blockHeight) {
        List<TransactionPo> txList = txDao.getTxs(blockHeight);
        for (TransactionPo tx : txList) {
            txDao.deleteTx(tx.getHash());
        }
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

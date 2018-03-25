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

import io.nuls.cache.service.intf.CacheService;
import io.nuls.consensus.utils.TxTimeComparator;
import io.nuls.core.chain.entity.*;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.dao.UtxoTransactionDataService;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.OutPutStatusEnum;
import io.nuls.ledger.entity.UtxoBalance;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.params.OperationType;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.TransferTransaction;
import io.nuls.ledger.entity.tx.UnlockNulsTransaction;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.util.UtxoTransactionTool;
import io.nuls.ledger.util.UtxoTransferTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    private static final String ORPHAN_TX_CACHE = "Orphan-tx-cache";
    @Autowired
    private CacheService<String, Transaction> txCacheService;

    private Lock lock = new ReentrantLock();

    @Override
    public void init() {
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        TransactionPo po = txDao.gettx(hash.getDigestHex());
        if (null == po) {
            return null;
        }
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
        if (null == po) {
            return null;
        }
        try {
            return UtxoTransferTool.toTransaction(po);
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }

    @Override
    public List<Transaction> getTxList(String address, int txType) throws Exception {
        if (StringUtils.isBlank(address) && txType == 0) {
            throw new NulsRuntimeException(ErrorCode.PARAMETER_ERROR);
        }
        List<Transaction> txList = new ArrayList<>();
        List<Transaction> cacheTxList = getCacheTxList(address, txType);
        txList.addAll(cacheTxList);

        if (StringUtils.isNotBlank(address) && NulsContext.LOCAL_ADDRESS_LIST.contains(address)) {
            List<TransactionLocalPo> poList = txDao.getLocalTxs(null, address, txType, 0, 0);
            for (TransactionLocalPo po : poList) {
                txList.add(UtxoTransferTool.toTransaction(po));
            }
        } else {
            List<TransactionPo> poList = txDao.getTxs(null, address, txType, 0, 0);
            for (TransactionPo po : poList) {
                txList.add(UtxoTransferTool.toTransaction(po));
            }
        }
        return txList;
    }

    @Override
    public long getTxCount(String address, int txType) throws Exception {
        int count = 0;
        List<Transaction> cacheTxList = getCacheTxList(address, txType);
        count += cacheTxList.size();

        boolean isLocal = NulsContext.LOCAL_ADDRESS_LIST.contains(address);
        if (isLocal) {
            count += txDao.getLocalTxsCount(address, txType);
        } else {
            count += txDao.getTxsCount(address, txType);
        }
        return count;
    }

    @Override
    public List<Transaction> getTxList(Long blockHeight, String address, int txType, int pageNumber, int pageSize) throws Exception {
        List<Transaction> txList = null;
        if (StringUtils.isNotBlank(address) && NulsContext.LOCAL_ADDRESS_LIST.contains(address)) {
            txList = getLocalTxList(blockHeight, address, txType, pageNumber, pageSize);
        } else {
            txList = new ArrayList<>();
            List<Transaction> cacheTxList = getCacheTxList(address, txType);
            txList.addAll(cacheTxList);

            List<TransactionPo> poList;
            if (pageNumber == 0 && pageSize == 0) {
                poList = txDao.getTxs(blockHeight, address, txType, 0, 0);
                for (TransactionPo po : poList) {
                    txList.add(UtxoTransferTool.toTransaction(po));
                }
                return txList;
            }

            int start = (pageNumber - 1) * pageSize;
            if (txList.size() >= start + pageSize) {
                return txList.subList(start, start + pageSize);
            } else if (start < txList.size()) {
                txList = txList.subList(start, txList.size());
                start = 0;
                pageSize = pageSize - txList.size();
            } else {
                start = start - txList.size();
            }
            poList = txDao.getTxs(blockHeight, address, txType, start, pageSize);
            for (TransactionPo po : poList) {
                txList.add(UtxoTransferTool.toTransaction(po));
            }
        }
        return txList;
    }

    private List<Transaction> getCacheTxList(String address, int txType) throws NulsException {
        List<Transaction> cacheTxList = new ArrayList<>(txCacheService.getElementList(CONFIRM_TX_CACHE));
        cacheTxList.addAll(txCacheService.getElementList(RECEIVE_TX_CACHE));
        cacheTxList.addAll(txCacheService.getElementList(ORPHAN_TX_CACHE));
        for (int i = cacheTxList.size() - 1; i >= 0; i--) {
            Transaction tx = cacheTxList.get(i);
            if (txType > 0 && tx.getType() != txType) {
                cacheTxList.remove(i);
                continue;
            }
            if (StringUtils.isNotBlank(address) && !checkTxIsMine(tx)) {
                cacheTxList.remove(i);
            }
        }
        Collections.sort(cacheTxList, TxTimeComparator.getInstance());
        return cacheTxList;
    }

    public List<Transaction> getLocalTxList(Long blockHeight, String address, int txType, int pageNumber, int pageSize) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        List<Transaction> cacheTxList = getCacheTxList(address, txType);
        txList.addAll(cacheTxList);

        List<TransactionLocalPo> poList;
        if (pageNumber == 0 && pageSize == 0) {
            poList = txDao.getLocalTxs(blockHeight, address, txType, pageNumber, pageSize);
            for (TransactionLocalPo po : poList) {
                txList.add(UtxoTransferTool.toTransaction(po));
            }
            return txList;
        }

        int start = (pageNumber - 1) * pageSize;
        if (txList.size() >= start + pageSize) {
            return txList.subList(start, start + pageSize);
        } else if (start < txList.size()) {
            txList = txList.subList(start, txList.size());
            start = 0;
            pageSize = pageSize - txList.size();
        } else {
            start = start - txList.size();
        }
        poList = txDao.getLocalTxs(blockHeight, address, txType, start, pageSize);
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
    public Page<Transaction> getTxList(Long height, int type, int pageNum, int pageSize) throws Exception {
        Page<TransactionPo> poPage = txDao.getTxs(height, type, pageNum, pageSize);
        Page<Transaction> txPage = new Page<>(poPage);
        List<Transaction> txList = new ArrayList<>();
        for (TransactionPo po : poPage.getList()) {
            txList.add(UtxoTransferTool.toTransaction(po));
        }
        txPage.setList(txList);
        return txPage;
    }

    @Override
    public Balance getBalance(String address) {
        if (StringUtils.isNotBlank(address)) {
            Balance balance = ledgerCacheService.getBalance(address);
            return balance;
        } else {
            Balance allBalance = new Balance();
            long usable = 0;
            long locked = 0;
            for (String addr : NulsContext.LOCAL_ADDRESS_LIST) {
                Balance balance = ledgerCacheService.getBalance(addr);
                if (null != balance) {
                    usable += balance.getUsable().getValue();
                    locked += balance.getLocked().getValue();
                }
            }
            allBalance.setUsable(Na.valueOf(usable));
            allBalance.setLocked(Na.valueOf(locked));
            allBalance.setBalance(Na.valueOf(usable + locked));
            return allBalance;
        }
    }

    @Override
    public Na getTxFee(int txType) {
        Block bestBlock = NulsContext.getInstance().getBestBlock();
        if (null == bestBlock) {
            return LedgerConstant.TRANSACTION_FEE;
        }
        long blockHeight = bestBlock.getHeader().getHeight();
        if (txType == TransactionConstant.TX_TYPE_COIN_BASE ||
                txType == TransactionConstant.TX_TYPE_SMALL_CHANGE ||
                txType == TransactionConstant.TX_TYPE_EXIT_CONSENSUS
                ) {
            return Na.ZERO;
        }
        long x = blockHeight / LedgerConstant.BLOCK_COUNT_OF_YEAR + 1;
        return LedgerConstant.TRANSACTION_FEE.div(x);
    }

    @Override
    public Result transfer(String address, String password, String toAddress, Na amount, String remark) {
        CoinTransferData coinData = new CoinTransferData(OperationType.TRANSFER, amount, address, toAddress, getTxFee(TransactionConstant.TX_TYPE_TRANSFER));
        return transfer(coinData, password, remark);
    }

    private Result transfer(CoinTransferData coinData, String password, String remark) {
        TransferTransaction tx = null;
        try {
            tx = UtxoTransactionTool.getInstance().createTransferTx(coinData, password, remark);
            ValidateResult result = tx.verify();
            if (!result.getErrorCode().getCode().equals(ErrorCode.SUCCESS.getCode())) {
                throw new NulsException(ErrorCode.FAILED);
            }
            byte[] txbytes = tx.serialize();
            TransferTransaction new_tx = new NulsByteBuffer(txbytes).readNulsData(new TransferTransaction());
            result = new_tx.verify();
            if (!result.getErrorCode().getCode().equals(ErrorCode.SUCCESS.getCode())) {
                throw new NulsException(ErrorCode.FAILED);
            }
            TransactionEvent event = new TransactionEvent();
            event.setEventBody(tx);
            eventBroadcaster.broadcastAndCacheAysn(event, true);
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, e.getMessage());
        }
        return new Result(true, "OK", tx.getHash().getDigestHex());
    }

    @Override
    public Result transfer(List<String> addressList, String password, String toAddress, Na amount, String remark) {
        CoinTransferData coinData = new CoinTransferData(OperationType.TRANSFER, amount, addressList, toAddress, getTxFee(TransactionConstant.TX_TYPE_TRANSFER));
        return transfer(coinData, password, remark);
    }


    @Override
    public Result lock(String address, String password, Na amount, long unlockTime, String remark) {
        LockNulsTransaction tx = null;
        try {
            CoinTransferData coinData = new CoinTransferData(OperationType.LOCK, amount, address, getTxFee(TransactionConstant.TX_TYPE_LOCK));
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
                boolean isMine = false;
                try {
                    isMine = this.checkTxIsMine(tx);
                } catch (NulsException e) {
                    throw new NulsRuntimeException(e);
                }
                TransactionPo po = UtxoTransferTool.toTransactionPojo(tx);
                poList.add(po);
                if (isMine) {
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
        List<TransactionPo> poList = txDao.getTxs(null, address, 0, 0, 0);
        if (poList.isEmpty()) {
            return;
        }
        List<TransactionLocalPo> localPoList = new ArrayList<>();

        for (TransactionPo po : poList) {
            TransactionLocalPo localPo = txDao.getLocaltx(po.getHash());
            if (localPo != null) {
                continue;
            }

            localPo = new TransactionLocalPo(po);
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
        if (tx.getStatus() != TxStatusEnum.AGREED) {
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

    @Override
    public long getBlockReward(long blockHeight) {
        return txDao.getBlockReward(blockHeight);
    }

    @Override
    public long getBlockFee(Long blockHeight) {
        return txDao.getBlockFee(blockHeight);
    }

    @Override
    public void unlockTxApprove(String txHash) {
        boolean b = true;
        int index = 0;
        while (b) {
            UtxoOutput output = ledgerCacheService.getUtxo(txHash + "-" + index);
            if (output != null) {
                if (OutPutStatusEnum.UTXO_UNCONFIRM_CONSENSUS_LOCK == output.getStatus()) {
                    output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND);
                } else if (OutPutStatusEnum.UTXO_CONFIRM_CONSENSUS_LOCK == output.getStatus()) {
                    output.setStatus(OutPutStatusEnum.UTXO_CONFIRM_UNSPEND);
                }
                index++;
            } else {
                b = false;
            }
        }
    }

    @Override
    @DbSession
    public void unlockTxSave(String txHash) {
        txDao.unlockTxOutput(txHash);
    }

    @Override
    @DbSession
    public void unlockTxRollback(String txHash) {
        boolean b = true;
        int index = 0;
        while (b) {
            UtxoOutput output = ledgerCacheService.getUtxo(txHash + "-" + index);
            if (output != null) {
                if (OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND == output.getStatus()) {
                    output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_CONSENSUS_LOCK);
                } else if (OutPutStatusEnum.UTXO_CONFIRM_UNSPEND == output.getStatus()) {
                    output.setStatus(OutPutStatusEnum.UTXO_CONFIRM_CONSENSUS_LOCK);
                }
                index++;
            } else {
                b = false;
            }
        }
        txDao.lockTxOutput(txHash);
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

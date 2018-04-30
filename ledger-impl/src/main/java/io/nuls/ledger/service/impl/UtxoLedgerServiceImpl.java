/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.ledger.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.Result;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.*;
import io.nuls.db.entity.*;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.*;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.params.OperationType;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.TransferTransaction;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.util.UtxoTransactionTool;
import io.nuls.ledger.util.UtxoTransferTool;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.constant.TxStatusEnum;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.TransactionEvent;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.Na;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.service.intf.TransactionService;
import io.nuls.protocol.utils.TransactionManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class UtxoLedgerServiceImpl implements LedgerService {

    @Autowired
    private TransactionDataService txDao;
    @Autowired
    TransactionLocalDataService localTxDao;
    @Autowired
    private UtxoInputDataService inputDataService;
    @Autowired
    private UtxoOutputDataService outputDataService;
    @Autowired
    private EventBroadcaster eventBroadcaster;
    @Autowired
    private BlockHeaderService blockHeaderDao;
    @Autowired
    private TxAccountRelationDataService relationDataService;

    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();

    private Lock lock = new ReentrantLock();

    @Override
    public Transaction getTx(NulsDigestData hash) {
        TransactionLocalPo localPo = localTxDao.get(hash.getDigestHex());
        if (localPo != null) {
            try {
                Transaction tx = UtxoTransferTool.toTransaction(localPo);
                return tx;
            } catch (Exception e) {
                Log.error(e);
            }
        }
        TransactionPo po = txDao.get(hash.getDigestHex());
        if (null == po) {
            return null;
        }
        try {
            Transaction tx = UtxoTransferTool.toTransaction(po);
            return tx;
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }

    @Override
    public Transaction getTx(String fromHash, int fromIndex) {
        UtxoInputPo inputPo = inputDataService.getByFromHash(fromHash, fromIndex);
        if (inputPo == null) {
            return null;
        }
        TransactionPo po = txDao.get(inputPo.getTxHash());
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

        if (StringUtils.isNotBlank(address) && NulsContext.LOCAL_ADDRESS_LIST.contains(address)) {
            List<TransactionLocalPo> poList = localTxDao.getTxs(null, address, txType, 0, 0);
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
    public long getTxCount(Long blockHeight, String address, int txType) throws Exception {
        long count = 0;
        boolean isLocal = NulsContext.LOCAL_ADDRESS_LIST.contains(address);
        if (isLocal) {
            count = localTxDao.getTxsCount(blockHeight, address, txType);
        } else {
            count = txDao.getTxsCount(blockHeight, address, txType);
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
            List<TransactionPo> poList;
            poList = txDao.getTxs(blockHeight, address, txType, pageNumber, pageSize);
            for (TransactionPo po : poList) {
                txList.add(UtxoTransferTool.toTransaction(po));
            }
        }
        return txList;
    }

    public List<Transaction> getLocalTxList(Long blockHeight, String address, int txType, int pageNumber, int pageSize) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        List<TransactionLocalPo> poList = localTxDao.getTxs(blockHeight, address, txType, pageNumber, pageSize);
        for (TransactionLocalPo po : poList) {
            txList.add(UtxoTransferTool.toTransaction(po));
        }

        List<Transaction> localTx = getWaitingTxList();
        Map<String, UtxoOutput> outputMap = new HashMap<>();
        for (Transaction tx : localTx) {
            AbstractCoinTransaction transaction = (AbstractCoinTransaction) tx;
            UtxoData utxoData = (UtxoData) transaction.getCoinData();

            for (UtxoOutput output : utxoData.getOutputs()) {
                outputMap.put(output.getKey(), output);
            }
        }
        for (Transaction tx : txList) {
            if (!(tx instanceof AbstractCoinTransaction)) {
                continue;
            }
            AbstractCoinTransaction transaction = (AbstractCoinTransaction) tx;
            UtxoData utxoData = (UtxoData) transaction.getCoinData();
            for (UtxoInput input : utxoData.getInputs()) {
                if (input.getFrom() == null) {
                    if (outputMap.containsKey(input.getKey())) {
                        input.setFrom(outputMap.get(input.getKey()));
                    }
                }
            }
        }
        return txList;
    }

    @Override
    public List<Transaction> getTxList(String blockHash) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        BlockHeaderPo header = blockHeaderDao.getHeader(blockHash);
        if (header == null) {
            return txList;
        }
        List<TransactionPo> poList = txDao.getTxs(header.getHeight());
        if (null == poList || poList.isEmpty()) {
            return txList;
        }
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
        Balance balance = ledgerCacheService.getBalance(address);
        if (balance == null) {
            return null;
        }
        long usable = 0;
        long locked = 0;

        List<UtxoOutput> unSpends = ledgerCacheService.getUnSpends(address);
        UtxoCoinManager.getInstance().filterUtxoByLocalTxs(address, unSpends);

        for (UtxoOutput output : unSpends) {
            if (output.isLocked(NulsContext.getInstance().getBestHeight())) {
                locked += output.getValue();
            } else {
                usable += output.getValue();
            }
        }

        balance.setUsable(Na.valueOf(usable));
        balance.setLocked(Na.valueOf(locked));
        balance.setBalance(Na.valueOf(usable + locked));
        return balance;
    }

    @Override
    public Balance getBalances() {
        Balance balances = new Balance();
        for (String address : NulsContext.LOCAL_ADDRESS_LIST) {
            Balance balance = getBalance(address);
            if (balance == null) {
                continue;
            }
            balances.setLocked(balances.getLocked().add(balance.getLocked()));
            balances.setUsable(balances.getUsable().add(balance.getUsable()));
        }
        balances.setBalance(balances.getLocked().add(balances.getUsable()));
        return balances;
    }

//    public Balance getBalance(String address) {
//        if (StringUtils.isNotBlank(address)) {
//            Balance balance = ledgerCacheService.getBalance(address);
//            return balance;
//        } else {
//            Balance allBalance = new Balance();
//            long usable = 0;
//            long locked = 0;
//            for (String addr : NulsContext.LOCAL_ADDRESS_LIST) {
//                Balance balance = ledgerCacheService.getBalance(addr);
//                if (null != balance) {
//                    usable += balance.getUsable().getValue();
//                    locked += balance.getLocked().getValue();
//                }
//            }
//            allBalance.setUsable(Na.valueOf(usable));
//            allBalance.setLocked(Na.valueOf(locked));
//            allBalance.setBalance(Na.valueOf(usable + locked));
//            return allBalance;
//        }
//    }

    @Override
    public Na getTxFee(int txType) {
        Block bestBlock = NulsContext.getInstance().getBestBlock();
        if (null == bestBlock) {
            return LedgerConstant.TRANSACTION_FEE;
        }
        long blockHeight = bestBlock.getHeader().getHeight();
        if (txType == TransactionConstant.TX_TYPE_COIN_BASE ||
                txType == TransactionConstant.TX_TYPE_SMALL_CHANGE
                || txType == TransactionConstant.TX_TYPE_CANCEL_DEPOSIT
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
            ValidateResult result = this.verifyTx(tx, this.getWaitingTxList());
            if (result.isFailed()) {
                throw new NulsException(result.getErrorCode());
            }
            this.saveLocalTx(tx);
            TransactionEvent event = new TransactionEvent();
            event.setEventBody(tx);
            eventBroadcaster.publishToLocal(event);
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
            coinData.addTo(new Coin(address, amount, unlockTime, 0));
            tx = UtxoTransactionTool.getInstance().createLockNulsTx(coinData, password, remark);

            tx.verify();
            TransactionEvent event = new TransactionEvent();
            event.setEventBody(tx);
            eventBroadcaster.publishToLocal(event);

        } catch (Exception e) {
            Log.error(e);
            try {
                rollbackTx(tx, null);
            } catch (NulsException e1) {
                Log.error(e1);
            }
            return new Result(false, e.getMessage());
        }

        return new Result(true, "OK", tx.getHash().getDigestHex());
    }

    @Override
    @DbSession
    public void saveTxList(List<Transaction> txList, long blockHeight) throws IOException {
        lock.lock();
        try {
            boolean isMine;
            List<TransactionPo> poList = new ArrayList<>();
            List<TransactionLocalPo> localPoList = new ArrayList<>();
            for (int i = 0; i < txList.size(); i++) {
                isMine = false;
                Transaction tx = txList.get(i);
                TransactionPo po = UtxoTransferTool.toTransactionPojo(tx, blockHeight);
                poList.add(po);
                if (tx.getType() == TransactionConstant.TX_TYPE_CANCEL_DEPOSIT) {
                    TransactionLocalPo localPo = localTxDao.get(tx.getHash().getDigestHex());
                    if (localPo != null) {
                        localPo.setTxStatus(TransactionLocalPo.CONFIRM);
                        localPo.setBlockHeight(tx.getBlockHeight());
                        localTxDao.update(localPo);
                        continue;
                    }
                } else {
                    isMine = this.checkTxIsMine(tx);
                }
                if (isMine) {
                    TransactionLocalPo localPo = UtxoTransferTool.toLocalTransactionPojo(tx);
                    localPoList.add(localPo);
                }
            }
            int successCount = txDao.save(poList);
            if (successCount != poList.size()) {
                throw new NulsRuntimeException(ErrorCode.FAILED, "save block txs fail , totalCount : " + poList.size() + " , successCount : " + successCount);
            }

            for (TransactionLocalPo localPo : localPoList) {
                TransactionLocalPo po = localTxDao.get(localPo.getHash());
                localPo.setTxStatus(TransactionLocalPo.CONFIRM);
                if (po != null) {
                    localTxDao.update(localPo);
                } else {
                    localTxDao.save(localPo);
                }
            }
        } catch (Exception e) {
            Log.error(e);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean saveLocalTx(Transaction tx) throws IOException {
        try {
            ValidateResult validateResult = this.conflictDetectTx(tx, this.getWaitingTxList());
            if (validateResult.isFailed()) {
                throw new NulsRuntimeException(validateResult.getErrorCode(), validateResult.getMessage());
            }
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        TransactionLocalPo localPo = UtxoTransferTool.toLocalTransactionPojo(tx);
        localPo.setTxStatus(TransactionLocalPo.UNCONFIRM);
        localTxDao.save(localPo);
        // save relation
        if (tx instanceof AbstractCoinTransaction) {
            AbstractCoinTransaction abstractTx = (AbstractCoinTransaction) tx;
            UtxoData utxoData = (UtxoData) abstractTx.getCoinData();
            if (utxoData.getInputs() != null && !utxoData.getInputs().isEmpty()) {
                UtxoInput input = utxoData.getInputs().get(0);
                UtxoOutput output = input.getFrom();
                TxAccountRelationPo relationPo = new TxAccountRelationPo();
                relationPo.setTxHash(tx.getHash().getDigestHex());
                relationPo.setAddress(output.getAddress());
                relationDataService.save(relationPo);
            }
        }
//        if (tx instanceof AbstractCoinTransaction) {
//            AbstractCoinTransaction abstractTx = (AbstractCoinTransaction) tx;
//            UtxoData utxoData = (UtxoData) abstractTx.getCoinData();
//
//            for (UtxoOutput output : utxoData.getOutputs()) {
//                if (output.isUsable() && NulsContext.LOCAL_ADDRESS_LIST.contains(output.getAddress())) {
//                    output.setTxHash(tx.getHash());
//                    ledgerCacheService.putUtxo(output.getKey(), output, false);
//                }
//            }
//        }
        return true;
    }

    @Override
    @DbSession
    public void removeLocalTxs(String address) {
        List<TxAccountRelationPo> relationList = relationDataService.selectByAddress(address);
        for (TxAccountRelationPo po : relationList) {
            localTxDao.delete(po.getTxHash());
        }
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
            TransactionLocalPo localPo = localTxDao.get(po.getHash());
            if (localPo != null) {
                continue;
            }

            localPo = new TransactionLocalPo(po, Transaction.TRANSFER_RECEIVE);
            for (UtxoInputPo inputPo : po.getInputs()) {
                if (inputPo.getFromOutPut().getAddress().equals(address)) {
                    localPo.setTransferType(Transaction.TRANSFER_SEND);
                    break;
                }
            }
            localPo.setTxStatus(TransactionLocalPo.CONFIRM);
            localTxDao.save(localPo);
            //localPoList.add(localPo);
        }
        //localTxDao.save(localPoList);
    }


    @Override
    public boolean checkTxIsMine(Transaction tx) {
        if (tx instanceof AbstractCoinTransaction) {
            return UtxoTransactionTool.getInstance().isMine((AbstractCoinTransaction) tx);
        }
        return false;
    }

    @Override
    public boolean checkTxIsMine(Transaction tx, String address) {
        if (tx instanceof AbstractCoinTransaction) {
            return UtxoTransactionTool.getInstance().isMine((AbstractCoinTransaction) tx, address);
        }
        return false;
    }

    @Override
    public boolean checkTxIsMySend(Transaction tx) {
        if (tx instanceof AbstractCoinTransaction) {
            return UtxoTransactionTool.getInstance().isMySend((AbstractCoinTransaction) tx);
        }
        return false;
    }

    @Override
    public boolean checkTxIsMySend(Transaction tx, String address) {
        if (tx instanceof AbstractCoinTransaction) {
            return UtxoTransactionTool.getInstance().isMySend((AbstractCoinTransaction) tx, address);
        }
        return false;
    }

    @Override

    public void rollbackTx(Transaction tx, Block block) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        BlockLog.debug("rollback tx:{}", tx.getHash());
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onRollback(tx, block);
        }
        tx.setStatus(TxStatusEnum.UNCONFIRM);
    }

    @Override
    @DbSession
    public void commitTx(Transaction tx, Block block) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
//        if (tx.getStatus() != TxStatusEnum.AGREED) {
//            return;
//        }
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        List<TransactionService> commitedServiceList = new ArrayList<>();
        try {
            for (TransactionService service : serviceList) {
                service.onCommit(tx, block);
                commitedServiceList.add(service);
            }
        } catch (Exception e) {
            Log.error(e);
            for (int i = commitedServiceList.size() - 1; i >= 0; i--) {
                TransactionService service = commitedServiceList.get(i);
                service.onRollback(tx, block);
            }
            throw e;
        }
        tx.setStatus(TxStatusEnum.CONFIRMED);
    }

    @Override
    public ValidateResult conflictDetectTx(Transaction tx, List<Transaction> txList) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        if (null == txList || txList.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        ValidateResult result = null;

        for (TransactionService service : serviceList) {
            result = service.conflictDetect(tx, txList);
            if (result.isFailed()) {
                break;
            }
        }

        return result;
    }

    @Override
    public void deleteTx(Transaction tx) {
        txDao.delete(tx.getHash().getDigestHex());
    }

    @Override
    @DbSession
    public void deleteTx(long blockHeight) {
        List<TransactionPo> txList = txDao.getTxs(blockHeight);
        for (TransactionPo tx : txList) {
            txDao.delete(tx.getHash());
            TransactionLocalPo localPo = localTxDao.get(tx.getHash());
            if (localPo != null) {
                if (localPo.getType() == TransactionConstant.TX_TYPE_COIN_BASE) {
                    localTxDao.delete(localPo.getHash());
                }
                localPo.setTxStatus(TransactionLocalPo.UNCONFIRM);
                localTxDao.update(localPo);
            }
        }
    }

    @Override
    public long getBlockReward(long blockHeight) {
        return outputDataService.getRewardByBlockHeight(blockHeight);
    }

    @Override
    public Long getBlockFee(Long blockHeight) {
        return txDao.getFeeByHeight(blockHeight);
    }

    @Override
    public long getLastDayTimeReward() {
        return outputDataService.getLastDayTimeReward();
    }

    @Override
    public long getAccountReward(String address, long lastTime) {
        return outputDataService.getAccountReward(address, lastTime);
    }

    @Override
    public long getAgentReward(String address, int type) {
        return outputDataService.getAgentReward(address, type);
    }

    @Override
    public void unlockTxApprove(String txHash) {
//        boolean b = true;
//        int index = 0;
//        while (b) {
//            UtxoOutput output = ledgerCacheService.getUtxo(txHash + "-" + index);
//            if (output != null) {
//                if (OutPutStatusEnum.UTXO_CONFIRMED_CONSENSUS_LOCK == output.getStatus()) {
//                    output.setStatus(OutPutStatusEnum.UTXO_CONFIRMED_UNSPENT);
//                }
//                UtxoTransactionTool.getInstance().calcBalance(output.getAddress(), false);
//                index++;
//            } else {
//                b = false;
//            }
//        }
    }

    @Override
    @DbSession
    public void unlockTxSave(String txHash) {
        Log.debug("-------------- exit agent unlockTxSave  ------------------txHash:" + txHash);
        outputDataService.unlockTxOutput(txHash);
        String key = txHash + "-" + 0;
        UtxoOutput output = ledgerCacheService.getUtxo(key);
        output.setStatus(OutPutStatusEnum.UTXO_UNSPENT);
        ledgerCacheService.putUtxo(key, output, true);
//        UtxoTransactionTool.getInstance().calcBalance(output.getAddress(), false);
    }

    @Override
    @DbSession
    public void unlockTxRollback(String txHash) {
        Log.debug("-------------- exit agent unlockTxRollback  ------------------txHash:" + txHash);
        outputDataService.lockTxOutput(txHash);
        Map<String, Object> keyMap = new HashMap<>();

        keyMap.put("txHash", txHash);
        keyMap.put("outIndex", 0);
        UtxoOutputPo po = outputDataService.get(keyMap);
        if (po != null) {
            po.setStatus(UtxoOutputPo.LOCKED);
            outputDataService.update(po);
            UtxoOutput output = UtxoTransferTool.toOutput(po);
            ledgerCacheService.putUtxo(output.getKey(), output, true);
        }
    }

    @Override
    public Page<UtxoOutput> getLockUtxo(String address, Integer pageNumber, Integer pageSize) {
        List<UtxoOutput> lockOutputs = new ArrayList<>();
        long count = outputDataService.getLockUtxoCount(address, TimeService.currentTimeMillis(), NulsContext.getInstance().getBestHeight());
        List<AbstractCoinTransaction> localTxs = UtxoCoinManager.getInstance().getLocalUnConfirmTxs();
        for (int i = localTxs.size() - 1; i >= 0; i--) {
            AbstractCoinTransaction tx = localTxs.get(i);
            UtxoData utxoData = (UtxoData) tx.getCoinData();
            for (int j = utxoData.getOutputs().size() - 1; j >= 0; j--) {
                UtxoOutput output = utxoData.getOutputs().get(j);
                if (output.isLocked(NulsContext.getInstance().getBestHeight()) && NulsContext.LOCAL_ADDRESS_LIST.contains(output.getAddress())) {
                    output.setCreateTime(tx.getTime());
                    output.setTxType(tx.getType());
                    lockOutputs.add(output);
                }
            }
        }

        int start = (pageNumber - 1) * pageSize;
        if (lockOutputs.size() >= start + pageSize) {
            lockOutputs = lockOutputs.subList(start, start + pageSize);
            Page page = new Page(pageNumber, pageSize);
            page.setList(lockOutputs);
            page.setTotal(count);
            return page;
        } else if (start < lockOutputs.size()) {
            lockOutputs = lockOutputs.subList(start, lockOutputs.size());
            start = 0;
            pageSize = pageSize - lockOutputs.size();
        } else {
            start = start - lockOutputs.size();
        }

        Collections.reverse(lockOutputs);
        List<UtxoOutputPo> poList = outputDataService.getLockUtxo(address, TimeService.currentTimeMillis(), NulsContext.getInstance().getBestHeight(), start, pageSize);
        for (UtxoOutputPo po : poList) {
            lockOutputs.add(UtxoTransferTool.toOutput(po));
        }
        Page page = new Page(pageNumber, pageSize);
        page.setTotal(count);
        page.setList(lockOutputs);
        return page;
    }

    @Override
    public Balance getAccountUtxo(String address, Na amount) {
        UtxoBalance balance = new UtxoBalance();
        List<UtxoOutput> unSpends = UtxoCoinManager.getInstance().getAccountUnSpend(address, amount);
        balance.setUnSpends(unSpends);
        return balance;
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

    @Override
    public void resetLedgerCache() {
//        ledgerCacheService.clear();
//        UtxoCoinManager.getInstance().cacheAllUnSpendUtxo();
    }

    @Override
    public List<Transaction> getWaitingTxList() throws NulsException {
        List<TransactionLocalPo> poList = localTxDao.getUnConfirmTxs();
        List<Transaction> txList = new ArrayList<>();
        for (TransactionLocalPo po : poList) {
            Transaction tx = UtxoTransferTool.toTransaction(po);
            txList.add(tx);
        }
        return txList;
    }

    @Override
    @DbSession
    public void deleteLocalTx(String txHash) {
        this.localTxDao.delete(txHash);
    }

    @Override
    public ValidateResult verifyTx(Transaction tx, List<Transaction> txList) {
        ValidateResult result = tx.verify();
        if (result.isFailed() && result.getErrorCode() == ErrorCode.ORPHAN_TX) {
            AbstractCoinTransaction coinTx = (AbstractCoinTransaction) tx;
            result = coinTx.getCoinDataProvider().verifyCoinData(coinTx, txList);
            if (result.isSuccess()) {
                coinTx.setSkipInputValidator(true);
                result = coinTx.verify();
                coinTx.setSkipInputValidator(false);
            }
        }
        return result;
    }

    @Override
    public void verifyTxWithException(Transaction tx, List<Transaction> txList) {
        ValidateResult result = this.verifyTx(tx, txList);
        if (result.isFailed()) {
            throw new NulsRuntimeException(result.getErrorCode(), result.getMessage());
        }
    }
}

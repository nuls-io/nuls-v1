/*
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
 *
 */

package io.nuls.account.ledger.base.service.impl;

import io.nuls.account.ledger.base.manager.BalanceManager;
import io.nuls.account.ledger.base.util.CoinComparator;
import io.nuls.account.ledger.base.util.TxInfoComparator;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.ledger.storage.po.TransactionInfoPo;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.account.service.AccountService;
import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.model.TransactionInfo;
import io.nuls.account.ledger.storage.service.AccountLedgerStorageService;

import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.kernel.utils.VarInt;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.constant.LedgerErrorCode;

import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.model.tx.TransferTransaction;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.TransactionService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Facjas
 * @date 2018/5/10.
 */
@Service
public class AccountLedgerServiceImpl implements AccountLedgerService, InitializingBean {

    @Autowired
    private AccountLedgerStorageService storageService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private BalanceManager balanceManager;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private BlockService blockService;

    private Lock lock = new ReentrantLock();
    private Lock saveLock = new ReentrantLock();

    @Override
    public void afterPropertiesSet() throws NulsException {
        init();
    }

    @Override
    public Result<Integer> saveConfirmedTransaction(Transaction tx) {
        return saveTransaction(tx, TransactionInfo.CONFIRMED);
    }

    @Override
    public Result<Integer> saveUnconfirmedTransaction(Transaction tx) {
        saveLock.lock();
        try {
            ValidateResult result1 = tx.verify();
            if (result1.isFailed()) {
                return result1;
            }
            result1 = this.ledgerService.verifyCoinData(tx, this.getAllUnconfirmedTransaction().getData());
            if (result1.isFailed()) {
                return result1;
            }
            return saveTransaction(tx, TransactionInfo.UNCONFIRMED);
        } finally {
            saveLock.unlock();
        }
    }

    @Override
    public Result<Integer> saveConfirmedTransactionList(List<Transaction> txs) {
        List<Transaction> savedTxList = new ArrayList<>();
        Result result;
        for (int i = 0; i < txs.size(); i++) {
            result = saveConfirmedTransaction(txs.get(i));
            if (result.isSuccess()) {
                savedTxList.add(txs.get(i));
            } else {
                rollback(savedTxList, false);
                return result;
            }
        }
        return Result.getSuccess().setData(savedTxList.size());
    }

    @Override
    public Result<Transaction> getUnconfirmedTransaction(NulsDigestData hash) {
        return storageService.getTempTx(hash);
    }

    @Override
    public Result<List<Transaction>> getAllUnconfirmedTransaction() {
        List<Transaction> localTxList = storageService.loadAllTempList().getData();
        return Result.getSuccess().setData(localTxList);
    }

    @Override
    public Result<Integer> rollback(Transaction tx) {
        if (!isLocalTransaction(tx)) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        Result result = storageService.deleteTxInfo(txInfoPo);

        if (result.isFailed()) {
            return result;
        }
        result = deleteLocalTx(tx);

        return result;
    }

    @Override
    public Result<Integer> rollback(List<Transaction> txs) {
        return rollback(txs, true);
    }

    public Result<Integer> rollback(List<Transaction> txs, boolean isCheckMine) {
        List<Transaction> txListToRollback;
        if (isCheckMine) {
            txListToRollback = getLocalTransaction(txs);
        } else {
            txListToRollback = txs;
        }
        for (int i = txListToRollback.size() - 1; i >= 0; i--) {
            rollback(txListToRollback.get(i));
        }

        return Result.getSuccess().setData(new Integer(txListToRollback.size()));
    }

    @Override
    public Result<Balance> getBalance(byte[] address) throws NulsException {
        if (address == null || address.length != AddressTool.HASH_LENGTH) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR);
        }

        if (!isLocalAccount(address)) {
            return Result.getFailed(AccountLedgerErrorCode.ACCOUNT_NOT_EXIST);
        }

        Balance balance = balanceManager.getBalance(address).getData();

        if (balance == null) {
            return Result.getFailed(AccountLedgerErrorCode.ACCOUNT_NOT_EXIST);
        }

        return Result.getSuccess().setData(balance);
    }

    @Override
    public CoinDataResult getCoinData(byte[] address, Na amount, int size) throws NulsException {
        lock.lock();
        try {
            CoinDataResult coinDataResult = new CoinDataResult();
            List<Coin> rawCoinList = storageService.getCoinList(address);
            List<Coin> coinList = new ArrayList<>();
            if (rawCoinList.isEmpty()) {
                coinDataResult.setEnough(false);
                return coinDataResult;
            }
            Collections.sort(rawCoinList, CoinComparator.getInstance());

            Set<byte[]> usedKeyset = getTmpUsedCoinKeySet();
            for (Coin coin : rawCoinList) {
                if (!usedKeyset.contains(coin.getOwner())) {
                    coinList.add(coin);
                }
            }

            boolean enough = false;
            List<Coin> coins = new ArrayList<>();
            Na values = Na.ZERO;
            //将所有余额从小到大排序后，累计未花费的余额
            for (int i = 0; i < coinList.size(); i++) {
                Coin coin = coinList.get(i);
                if (!coin.usable()) {
                    continue;
                }
                coins.add(coin);
                size += coin.size();
                if (i == 127) {
                    size += 1;
                }
                //每次累加一条未花费余额时，需要重新计算手续费
                Na fee = TransactionFeeCalculator.getFee(size);
                values = values.add(coin.getNa());
                if (values.isGreaterOrEquals(amount.add(fee))) {
                    //余额足够后，需要判断是否找零，如果有找零，则需要重新计算手续费
                    Na change = values.subtract(amount.add(fee));
                    if (change.isGreaterThan(Na.ZERO)) {
                        Coin changeCoin = new Coin();
                        changeCoin.setOwner(address);
                        changeCoin.setNa(change);

                        fee = TransactionFeeCalculator.getFee(size + changeCoin.size());
                        if (values.isLessThan(amount.add(fee))) {
                            continue;
                        }
                        coinDataResult.setChange(changeCoin);
                    }

                    enough = true;
                    coinDataResult.setEnough(true);
                    coinDataResult.setFee(fee);
                    coinDataResult.setCoinList(coins);
                    break;
                }
            }
            if (!enough) {
                coinDataResult.setEnough(false);
                return coinDataResult;
            }
            return coinDataResult;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Transaction getTxByOwner(byte[] owner) {
        //todo
        //byte[] txHash = new byte[NulsDigestData.
        return null;
    }

    @Override
    public boolean isLocalAccount(byte[] address) {
        List<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return false;
        }

        for (int i = 0; i < localAccountList.size(); i++) {
            if (Arrays.equals(localAccountList.get(i).getAddress().getBase58Bytes(), address)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Result transfer(byte[] from, byte[] to, Na values, String password, String remark) {
        try {
            AssertUtil.canNotEmpty(from, "the from address can not be empty");
            AssertUtil.canNotEmpty(to, "the to address can not be empty");
            AssertUtil.canNotEmpty(values, "the amount can not be empty");

            if (values.isZero() || values.isLessThan(Na.ZERO)) {
                return Result.getFailed("amount error");
            }

            Result<Account> accountResult = accountService.getAccount(from);
            if (accountResult.isFailed()) {
                return accountResult;
            }
            Account account = accountResult.getData();

            if (accountService.isEncrypted(account).isSuccess()) {
                AssertUtil.canNotEmpty(password, "the password can not be empty");

                Result passwordResult = accountService.validPassword(account, password);
                if (passwordResult.isFailed()) {
                    return passwordResult;
                }
            }

            TransferTransaction tx = new TransferTransaction();
            if (StringUtils.isNotBlank(remark)) {
                try {
                    tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            tx.setTime(TimeService.currentTimeMillis());
            CoinData coinData = new CoinData();
            Coin toCoin = new Coin(to, values);
            coinData.getTo().add(toCoin);

            CoinDataResult coinDataResult = getCoinData(from, values, tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(LedgerErrorCode.BALANCE_NOT_ENOUGH);
            }
            coinData.setFrom(coinDataResult.getCoinList());
            if (coinDataResult.getChange() != null) {
                coinData.getTo().add(coinDataResult.getChange());
            }
            tx.setCoinData(coinData);

            tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
            P2PKHScriptSig sig = new P2PKHScriptSig();
            sig.setPublicKey(account.getPubKey());
            sig.setSignData(accountService.signData(tx.getHash().serialize(), account, password));
            tx.setScriptSig(sig.serialize());


            Result saveResult = saveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                return saveResult;
            }
            Result sendResult = this.transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                return sendResult;
            }
            return Result.getSuccess().setData(tx.getHash().getDigestHex());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    @Override
    public Result unlockCoinData(Transaction tx, long newLockTime) {
        List<byte[]> addresses = getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess();
        }
        byte status = TransactionInfo.CONFIRMED;
        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(status);

        byte[] txHashBytes = new byte[0];
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // unlock utxo - to
            List<Coin> tos = coinData.getTo();
            byte[] indexBytes;
            Coin to, needUnLockUtxo;
            for (int i = 0, length = tos.size(); i < length; i++) {
                to = tos.get(i);
                if (to.getLockTime() == -1) {
                    Coin needUnLockUtxoNew = new Coin(to.getOwner(), to.getNa(), newLockTime);
                    needUnLockUtxoNew.setFrom(to.getFrom());
                    try {
                        byte[] outKey = org.spongycastle.util.Arrays.concatenate(to.getOwner(), tx.getHash().serialize(), new VarInt(i).encode());
                        storageService.saveUTXO(outKey, needUnLockUtxoNew.serialize());
                    } catch (IOException e) {
                        throw new NulsRuntimeException(e);
                    }
                    //todo , think about weather to add a transaction history
                    break;
                }
            }
        }
        return Result.getSuccess();
    }

    @Override
    public Result rollbackUnlockTxCoinData(Transaction tx) {
        List<byte[]> addresses = getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess();
        }
        byte status = TransactionInfo.CONFIRMED;
        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(status);

        byte[] txHashBytes = new byte[0];
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // lock utxo - to
            List<Coin> tos = coinData.getTo();
            for (int i = 0, length = tos.size(); i < length; i++) {
                if (tos.get(i).getLockTime() == -1) {
                    try {
                        byte[] outKey = org.spongycastle.util.Arrays.concatenate(tos.get(i).getOwner(), tx.getHash().serialize(), new VarInt(i).encode());
                        storageService.saveUTXO(outKey, tos.get(i).serialize());
                    } catch (IOException e) {
                        throw new NulsRuntimeException(e);
                    }
                    break;
                }
            }
        }
        return Result.getSuccess();
    }

    @Override
    public Result importAccountLedger(String address) {
        if (address == null || !Address.validAddress(address)) {
            return Result.getFailed(AccountLedgerErrorCode.ADDRESS_ERROR);
        }

        byte[] addressBytes = null;
        try {
            addressBytes = Base58.decode(address);
        } catch (Exception e) {
            return Result.getFailed(AccountLedgerErrorCode.ADDRESS_ERROR);
        }

        long start = 0;
        long end = NulsContext.getInstance().getBestHeight();
        while (start < end) {
            for (long i = start; i <= end; i++) {
                List<NulsDigestData> txs = blockService.getBlock(i).getData().getTxHashList();
                for (int j = 0; j < txs.size(); j++) {
                    Transaction tx = ledgerService.getTx(txs.get(j));
                    saveTransaction(tx, addressBytes, TransactionInfo.CONFIRMED);
                }
            }
            start = end;
            end = NulsContext.getInstance().getBestHeight();
        }
        try {
            balanceManager.refreshBalance(addressBytes);
        } catch (Exception e) {
            Log.info(address);
        }
        return Result.getSuccess();
    }

    @Override
    public Result<List<TransactionInfo>> getTxInfoList(byte[] address) {
        try {
            List<TransactionInfoPo> infoPoList = storageService.getTxInfoList(address);
            List<TransactionInfo> infoList = new ArrayList<>();
            for (TransactionInfoPo po : infoPoList) {
                infoList.add(po.toTransactionInfo());
            }

            Collections.sort(infoList, TxInfoComparator.getInstance());
            return Result.getSuccess().setData(infoList);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    @Override
    public Result<List<Coin>> getLockUtxo(byte[] address) {
        Result<List<Coin>> result = new Result<>();
        try {
            result.setSuccess(true);
            List<Coin> coinList = storageService.getCoinList(address);
            List<Coin> lockCoinList = new ArrayList<>();
            for (Coin coin : coinList) {
                if (coin != null && !coin.usable()) {
                    lockCoinList.add(coin);
                }
            }
            Collections.sort(coinList, CoinComparator.getInstance());
            result.setData(lockCoinList);
        } catch (NulsException e) {
            Log.error(e);
            result.setSuccess(false);
        }
        return result;
    }

    protected Result<Integer> saveTransaction(Transaction tx, byte status) {

        List<byte[]> addresses = getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(status);

        Result result = storageService.saveTxInfo(txInfoPo, addresses);

        if (result.isFailed()) {
            return result;
        }
        result = saveLocalTx(tx);
        if (result.isFailed()) {
            storageService.deleteTxInfo(txInfoPo);
        }

        if (status == TransactionInfo.UNCONFIRMED) {
            result = storageService.saveTempTx(tx);
        } else {
            storageService.deleteTempTx(tx);
        }
        for (int i = 0; i < addresses.size(); i++) {
            balanceManager.refreshBalance(addresses.get(i));
        }
        return result;
    }

    protected Result<Integer> saveTransaction(Transaction tx, byte[] address, byte status) {
        List<byte[]> destAddresses = new ArrayList<byte[]>();
        destAddresses.add(address);
        List<byte[]> addresses = getRelatedAddresses(tx, destAddresses);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(status);

        Result result = storageService.saveTxInfo(txInfoPo, addresses);

        if (result.isFailed()) {
            return result;
        }
        result = saveLocalTx(tx);
        if (result.isFailed()) {
            storageService.deleteTxInfo(txInfoPo);
        }
        return result;
    }


    protected List<Transaction> getLocalTransaction(List<Transaction> txs) {
        List<Transaction> resultTxs = new ArrayList<>();
        if (txs == null || txs.size() == 0) {
            return resultTxs;
        }
        List<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return resultTxs;
        }
        Transaction tmpTx;
        for (int i = 0; i < txs.size(); i++) {
            tmpTx = txs.get(i);
            if (isLocalTransaction(tmpTx)) {
                resultTxs.add(tmpTx);
            }
        }
        return resultTxs;
    }

    protected List<byte[]> getRelatedAddresses(Transaction tx) {
        List<byte[]> result = new ArrayList<>();
        if (tx == null) {
            return result;
        }
        List<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return result;
        }
        List<byte[]> destAddresses = new ArrayList<>();
        for (Account account : localAccountList) {
            destAddresses.add(account.getAddress().getBase58Bytes());
        }

        return getRelatedAddresses(tx, destAddresses);
    }

    protected List<byte[]> getRelatedAddresses(Transaction tx, List<byte[]> addresses) {
        List<byte[]> result = new ArrayList<>();
        if (tx == null) {
            return result;
        }
        if (addresses == null || addresses.size() == 0) {
            return result;
        }
        List<byte[]> sourceAddresses = tx.getAllRelativeAddress();
        if (sourceAddresses == null || sourceAddresses.size() == 0) {
            return result;
        }

        for (byte[] tempSourceAddress : sourceAddresses) {
            for (byte[] tempDestAddress : addresses) {
                if (Arrays.equals(tempDestAddress, tempSourceAddress)) {
                    result.add(tempSourceAddress);
                    continue;
                }
            }
        }
        return result;
    }

    protected boolean isLocalTransaction(Transaction tx) {

        if (tx == null) {
            return false;
        }
        List<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return false;
        }
        List<byte[]> addresses = tx.getAllRelativeAddress();
        for (int j = 0; j < addresses.size(); j++) {
            if (isLocalAccount(addresses.get(j))) {
                return true;
            }
        }
        return false;
    }

    public void init() {
    }

    public Result saveLocalTx(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] txHashBytes = new byte[0];
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        CoinData coinData = tx.getCoinData();

        if (coinData != null) {
            // delete - from
            List<Coin> froms = coinData.getFrom();
            Set<byte[]> fromsSet = new HashSet<>();
            for (Coin from : froms) {
                byte[] fromSource = from.getOwner();
                byte[] utxoFromSource = new byte[tx.getHash().size()];
                byte[] fromIndex = new byte[fromSource.length - utxoFromSource.length];
                System.arraycopy(fromSource, 0, utxoFromSource, 0, tx.getHash().size());
                System.arraycopy(fromSource, tx.getHash().size(), fromIndex, 0, fromIndex.length);
                Transaction sourceTx = null;
                try {
                    sourceTx = ledgerService.getTx(NulsDigestData.fromDigestHex(Hex.encode(fromSource)));
                } catch (Exception e) {
                    throw new NulsRuntimeException(e);
                }
                if (sourceTx == null) {
                    return Result.getFailed(AccountLedgerErrorCode.SOURCE_TX_NOT_EXSITS);
                }
                byte[] address = sourceTx.getCoinData().getTo().get((int) new VarInt(fromIndex, 0).value).getOwner();
                fromsSet.add(org.spongycastle.util.Arrays.concatenate(address, from.getOwner()));
            }
            storageService.batchDeleteUTXO(fromsSet);
            // save utxo - to
            List<Coin> tos = coinData.getTo();
            byte[] indexBytes;
            Map<byte[], byte[]> toMap = new HashMap<>();
            for (int i = 0, length = tos.size(); i < length; i++) {
                try {
                    byte[] outKey = org.spongycastle.util.Arrays.concatenate(tos.get(i).getOwner(), tx.getHash().serialize(), new VarInt(i).encode());
                    toMap.put(outKey, tos.get(i).serialize());
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }
            storageService.batchSaveUTXO(toMap);
        }
        return Result.getSuccess();
    }

    public Result deleteLocalTx(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] txHashBytes = new byte[0];
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        CoinData coinData = tx.getCoinData();

        if (coinData != null) {
            // delete - from
            List<Coin> froms = coinData.getFrom();
            Map<byte[], byte[]> fromMap = new HashMap<>();
            for (Coin from : froms) {
                byte[] fromSource = from.getOwner();
                byte[] utxoFromSource = new byte[tx.getHash().size()];
                byte[] fromIndex = new byte[fromSource.length - utxoFromSource.length];
                System.arraycopy(fromSource, 0, utxoFromSource, 0, tx.getHash().size());
                System.arraycopy(fromSource, tx.getHash().size(), fromIndex, 0, fromIndex.length);
                Transaction sourceTx = null;
                try {
                    sourceTx = ledgerService.getTx(NulsDigestData.fromDigestHex(Hex.encode(fromSource)));
                } catch (Exception e) {
                    throw new NulsRuntimeException(e);
                }
                if (sourceTx == null) {
                    return Result.getFailed(AccountLedgerErrorCode.SOURCE_TX_NOT_EXSITS);
                }
                byte[] address = sourceTx.getCoinData().getTo().get((int) new VarInt(fromIndex, 0).value).getOwner();
                try {
                    fromMap.put(org.spongycastle.util.Arrays.concatenate(address, from.getOwner()), sourceTx.getCoinData().getTo().get((int) new VarInt(fromIndex, 0).value).serialize());
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }
            storageService.batchSaveUTXO(fromMap);
            // save utxo - to
            List<Coin> tos = coinData.getTo();
            byte[] indexBytes;
            Set<byte[]> toSet = new HashSet<>();
            for (int i = 0, length = tos.size(); i < length; i++) {
                try {
                    byte[] outKey = org.spongycastle.util.Arrays.concatenate(tos.get(i).getOwner(), tx.getHash().serialize(), new VarInt(i).encode());
                    toSet.add(outKey);
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }
            storageService.batchDeleteUTXO(toSet);
        }
        return Result.getSuccess();
    }

    protected Set<byte[]> getTmpUsedCoinKeySet() {
        List<Transaction> localTxList = storageService.loadAllTempList().getData();
        Set<byte[]> coinKeys = new HashSet<>();
        for (Transaction tx : localTxList) {
            CoinData coinData = tx.getCoinData();
            List<Coin> coins = coinData.getFrom();
            for (Coin coin : coins) {
                byte[] owner = coin.getOwner();
                byte[] coinKey = new byte[owner.length - AddressTool.HASH_LENGTH];
                System.arraycopy(owner, AddressTool.HASH_LENGTH, coinKey, 0, coinKey.length);
                coinKeys.add(coin.getOwner());
            }
        }
        return coinKeys;
    }
}

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
import io.nuls.account.ledger.base.service.LocalUtxoService;
import io.nuls.account.ledger.base.service.TransactionInfoService;
import io.nuls.account.ledger.base.util.AccountLegerUtils;
import io.nuls.account.ledger.base.util.CoinComparator;
import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.model.TransactionInfo;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.ledger.storage.po.TransactionInfoPo;
import io.nuls.account.ledger.storage.service.UnconfirmedTransactionStorageService;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.account.service.AccountService;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.constant.ProtocolConstant;
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
    private LocalUtxoService localUtxoService;

    @Autowired
    private UnconfirmedTransactionStorageService unconfirmedTransactionStorageService;

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

    @Autowired
    private TransactionInfoService transactionInfoService;

    private Lock lock = new ReentrantLock();
    private Lock saveLock = new ReentrantLock();

    @Override
    public void afterPropertiesSet() throws NulsException {
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
                rollbackTransaction(savedTxList, false);
                return result;
            }
        }
        return Result.getSuccess().setData(savedTxList.size());
    }

    @Override
    public Result<Integer> saveConfirmedTransaction(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(TransactionInfo.CONFIRMED);

        Result result = transactionInfoService.saveTransactionInfo(txInfoPo, addresses);
        if (result.isFailed()) {
            return result;
        }

        // 判断交易是否已经存在，如果存在则不处理coin
        Transaction unconfirmedTx = unconfirmedTransactionStorageService.getUnconfirmedTx(tx.getHash()).getData();

        if (unconfirmedTx == null) {
            result = localUtxoService.saveUtxoForLocalAccount(tx);
            if (result.isFailed()) {
                transactionInfoService.deleteTransactionInfo(txInfoPo);
                return result;
            }
        } else {
            unconfirmedTransactionStorageService.deleteUnconfirmedTx(tx.getHash());
        }

        for (int i = 0; i < addresses.size(); i++) {
            balanceManager.refreshBalance(addresses.get(i));
        }
        return result;
    }

    @Override
    public Result<Integer> verifyAndSaveUnconfirmedTransaction(Transaction tx) {
        saveLock.lock();
        try {
            ValidateResult result = tx.verify();
            if (result.isFailed()) {
                return result;
            }
            if (!(tx.getType() == ConsensusConstant.TX_TYPE_YELLOW_PUNISH || tx.getType() == ProtocolConstant.TX_TYPE_COINBASE || tx.getType() == ConsensusConstant.TX_TYPE_RED_PUNISH)) {
                result = this.ledgerService.verifyCoinData(tx, this.getAllUnconfirmedTransaction().getData());
                if (result.isFailed()) {
                    Log.info("verifyCoinData failed");
                    return result;
                }
            }
            return saveUnconfirmedTransaction(tx);
        } finally {
            saveLock.unlock();
        }
    }

    protected Result saveUnconfirmedTransaction(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(TransactionInfo.UNCONFIRMED);

        Result result = transactionInfoService.saveTransactionInfo(txInfoPo, addresses);
        if (result.isFailed()) {
            return result;
        }

        result = localUtxoService.saveUtxoForLocalAccount(tx);
        if (result.isFailed()) {
            transactionInfoService.deleteTransactionInfo(txInfoPo);
            return result;
        }

        result = unconfirmedTransactionStorageService.saveUnconfirmedTx(tx.getHash(), tx);

        for (int i = 0; i < addresses.size(); i++) {
            balanceManager.refreshBalance(addresses.get(i));
        }
        return result;
    }



    @Override
    public Result<List<Transaction>> getAllUnconfirmedTransaction() {
        List<Transaction> localTxList = unconfirmedTransactionStorageService.loadAllUnconfirmedList().getData();
        return Result.getSuccess().setData(localTxList);
    }

    @Override
    public Result<Integer> rollbackTransaction(List<Transaction> txs) {
        return rollbackTransaction(txs, true);
    }

    public Result<Integer> rollbackTransaction(List<Transaction> txs, boolean isCheckMine) {
        List<Transaction> txListToRollback;
        if (isCheckMine) {
            txListToRollback = filterLocalTransaction(txs);
        } else {
            txListToRollback = txs;
        }
        for (int i = txListToRollback.size() - 1; i >= 0; i--) {
            rollbackTransaction(txListToRollback.get(i));
        }
        for (int i = 0; i < txListToRollback.size(); i++) {
            verifyAndSaveUnconfirmedTransaction(txListToRollback.get(i));
        }
        return Result.getSuccess().setData(new Integer(txListToRollback.size()));
    }

    @Override
    public Result<Integer> rollbackTransaction(Transaction tx) {
        if (!AccountLegerUtils.isLocalTransaction(tx)) {
            return Result.getSuccess().setData(new Integer(0));
        }

        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        Result result = transactionInfoService.deleteTransactionInfo(txInfoPo);

        if (result.isFailed()) {
            return result;
        }
        result = localUtxoService.deleteUtxoOfTransaction(tx);

        for (int i = 0; i < addresses.size(); i++) {
            balanceManager.refreshBalance(addresses.get(i));
        }

        return result;
    }

    @Override
    public Result<Balance> getBalance(byte[] address) throws NulsException {
        if (address == null || address.length != AddressTool.HASH_LENGTH) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR);
        }

        if (!AccountLegerUtils.isLocalAccount(address)) {
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
            List<Coin> coinList = balanceManager.getCoinListByAddress(address);
            if (coinList.isEmpty()) {
                coinDataResult.setEnough(false);
                return coinDataResult;
            }
            Collections.sort(coinList, CoinComparator.getInstance());

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
//            for(Coin coin : coinDataResult.getCoinList()) {
//                coin.setNa(Na.ZERO);
//                coin.setLockTime(0L);
//            }
            return coinDataResult;
        } finally {
            lock.unlock();
        }
    }


    public Na getTxFee(byte[] address, Na amount, int size) {
        List<Coin> coinList = balanceManager.getCoinListByAddress(address);
        if (coinList.isEmpty()) {
            return Na.ZERO;
        }
        Collections.sort(coinList, CoinComparator.getInstance());

        Na values = Na.ZERO;
        Na fee = null;
        for (int i = 0; i < coinList.size(); i++) {
            Coin coin = coinList.get(i);
            if (!coin.usable()) {
                continue;
            }
            size += coin.size();
            if (i == 127) {
                size += 1;
            }
            fee = TransactionFeeCalculator.getFee(size);
            values = values.add(coin.getNa());

            if (values.isGreaterOrEquals(amount.add(fee))) {
                Na change = values.subtract(amount.add(fee));
                if (change.isGreaterThan(Na.ZERO)) {
                    Coin changeCoin = new Coin();
                    changeCoin.setOwner(address);
                    changeCoin.setNa(change);

                    fee = TransactionFeeCalculator.getFee(size + changeCoin.size());
                    if (values.isLessThan(amount.add(fee))) {
                        continue;
                    }
                }
            }
        }
        return fee;
    }

    @Override
    public Result transfer(byte[] from, byte[] to, Na values, String password, String remark) {
        try {
            Result<Account> accountResult = accountService.getAccount(from);
            if (accountResult.isFailed()) {
                return accountResult;
            }
            Account account = accountResult.getData();

            if (accountService.isEncrypted(account).isSuccess() && account.isLocked()) {
                AssertUtil.canNotEmpty(password, "the password can not be empty");

                Result passwordResult = accountService.validPassword(account, password);
                if (passwordResult.isFailed()) {
                    return passwordResult;
                }
            }

            TransferTransaction tx = new TransferTransaction();
            try {
                tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
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

            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            P2PKHScriptSig sig = new P2PKHScriptSig();
            sig.setPublicKey(account.getPubKey());
            sig.setSignData(accountService.signData(tx.getHash().serialize(), account, password));
            tx.setScriptSig(sig.serialize());


            Result saveResult = verifyAndSaveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                return saveResult;
            }
            Result sendResult = transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                this.rollbackTransaction(tx);
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
    public Result transferFee(byte[] from, byte[] to, Na values, String remark) {
        Result<Account> accountResult = accountService.getAccount(from);
        if (accountResult.isFailed()) {
            return accountResult;
        }
        TransferTransaction tx = new TransferTransaction();
        try {
            tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        tx.setTime(TimeService.currentTimeMillis());
        CoinData coinData = new CoinData();
        Coin toCoin = new Coin(to, values);
        coinData.getTo().add(toCoin);

        Na fee = getTxFee(from, values, tx.size());
        return Result.getSuccess().setData(fee);
    }

    /**
     * 导入账户
     */
    @Override
    public Result importLedgerByAddress(String address) {
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
        while (start <= end) {
            for (long i = start; i <= end; i++) {
                List<Transaction> txs = blockService.getBlock(i).getData().getTxs();
                for (Transaction tx : txs) {
                    importConfirmedTransaction(tx, addressBytes);
                }
            }
            start = end;
            end = NulsContext.getInstance().getBestHeight();
            if (start == end) {
                break;
            }
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
        return transactionInfoService.getTxInfoList(address);
    }

    @Override
    public Result<List<Coin>> getLockedUtxo(byte[] address) {
        Result<List<Coin>> result = new Result<>();
        result.setSuccess(true);
        List<Coin> coinList = balanceManager.getCoinListByAddress(address);
        List<Coin> lockCoinList = new ArrayList<>();
        for (Coin coin : coinList) {
            if (coin != null && !coin.usable()) {
                lockCoinList.add(coin);
            }
        }
//        Collections.sort(coinList, CoinComparator.getInstance());
        result.setData(lockCoinList);
        return result;
    }

    protected Result<Integer> importConfirmedTransaction(Transaction tx, byte[] address) {

        if(!AddressTool.validAddress(address)){
            return Result.getFailed(AccountLedgerErrorCode.ADDRESS_ERROR);
        }

        if(!AccountLegerUtils.isTxRelatedToAddress(tx, address)){
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(TransactionInfo.CONFIRMED);

        List<byte[]> addresses = new ArrayList<>();
        addresses.add(address);
        Result result = transactionInfoService.saveTransactionInfo(txInfoPo, addresses);

        if (result.isFailed()) {
            return result;
        }
        result = localUtxoService.saveUtxoForAccount(tx, address);
        if (result.isFailed()) {
            transactionInfoService.deleteTransactionInfo(txInfoPo);
        }
        return result;
    }

    protected List<Transaction> filterLocalTransaction(List<Transaction> txs) {
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
            if (AccountLegerUtils.isLocalTransaction(tmpTx)) {
                resultTxs.add(tmpTx);
            }
        }
        return resultTxs;
    }

    @Override
    public Result unlockCoinData(Transaction tx, long newLockTime) {
        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess();
        }
        byte status = TransactionInfo.CONFIRMED;
        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(status);

        List<byte[]> addresses1 = localUtxoService.unlockCoinData(tx,newLockTime).getData();
        for(byte[] address: addresses1) {
            balanceManager.refreshBalance(address);
        }
        return Result.getSuccess();
    }

    @Override
    public Result rollbackUnlockTxCoinData(Transaction tx) {
        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess();
        }
        byte status = TransactionInfo.CONFIRMED;
        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(status);

        List<byte[]> addresses1 = localUtxoService.rollbackUnlockTxCoinData(tx).getData();
        for(byte[] address: addresses1) {
            balanceManager.refreshBalance(address);
        }

        return Result.getSuccess();
    }

    @Override
    public Result<Transaction> getUnconfirmedTransaction(NulsDigestData hash) {
        return unconfirmedTransactionStorageService.getUnconfirmedTx(hash);
    }
}

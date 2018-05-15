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

import io.nuls.account.ledger.base.util.CoinComparator;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.ledger.base.service.balance.BalanceService;
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
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.ledger.constant.LedgerErrorCode;

import io.nuls.protocol.model.tx.TransferTransaction;
import io.nuls.protocol.service.TransactionService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Facjas
 * @date 2018/5/10.
 */
@Component
public class AccountLedgerServiceImpl implements AccountLedgerService {

    @Autowired
    private AccountLedgerStorageService storageService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private TransactionService transactionService;

    private static List<Account> localAccountList;

    @Override
    public void init() {
        reloadAccount();
    }

    @Override
    public Result<Integer> saveConfirmedTransaction(Transaction tx) {
        return saveConfirmedTransaction(tx, TransactionInfo.CONFIRMED);
    }

    @Override
    public Result<Integer> saveUnconfirmedTransaction(Transaction tx) {
        return saveConfirmedTransaction(tx, TransactionInfo.UNCONFIRMED);
    }

    @Override
    public Result<Integer> saveConfirmedTransactionList(List<Transaction> txs) {
        List<Transaction> txListToSave = getLocalTransaction(txs);
        List<Transaction> savedTxList = new ArrayList<>();
        Result result;
        for (int i = 0; i < txListToSave.size(); i++) {
            result = saveConfirmedTransaction(txListToSave.get(i));
            if (result.isSuccess()) {
                savedTxList.add(txListToSave.get(i));
            } else {
                rollback(savedTxList, false);
                return result;
            }
        }
        return Result.getSuccess().setData(txListToSave.size());
    }

    @Override
    public Result<Integer> rollback(Transaction tx) {
        if (!isLocalTransaction(tx)) {
            return Result.getFailed().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        Result result = storageService.deleteLocalTxInfo(txInfoPo);

        if (result.isFailed()) {
            return result;
        }
        result = storageService.deleteLocalTx(tx);

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
        for (int i = 0; i < txListToRollback.size(); i++) {
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

        Balance balance = balanceService.getBalance(Base58.encode(address));

        if (balance == null) {
            return Result.getFailed(AccountLedgerErrorCode.ACCOUNT_NOT_EXIST);
        }

        return Result.getSuccess().setData(balance);
    }

    @Override
    public CoinDataResult getCoinData(byte[] address, Na amount, int size) throws NulsException {
        CoinDataResult coinDataResult = new CoinDataResult();
        List<Coin> coinList = storageService.getCoinBytes(address);
        if (coinList.isEmpty()) {
            coinDataResult.setEnough(false);
            return coinDataResult;
        }
        Collections.sort(coinList, CoinComparator.getInstance());

        boolean enough = false;
        List<Coin> coins = new ArrayList<>();
        Na values = Na.ZERO;
        for (int i = 0; i < coinList.size(); i++) {
            Coin coin = coinList.get(i);
            coins.add(coin);
            size += coin.size();
            Na fee = TransactionFeeCalculator.getFee(size);
            values = values.add(coin.getNa());
            if (values.isGreaterOrEquals(values.add(fee))) {
                enough = true;
                coinDataResult.setEnough(true);
                coinDataResult.setFee(fee);
                coinDataResult.setCoinList(coins);

                Na change = values.subtract(values.add(fee));
                if (change.isGreaterThan(Na.ZERO)) {
                    Coin changeCoin = new Coin();
                    changeCoin.setOwner(address);
                    changeCoin.setNa(change);
                }
                break;
            }
        }
        if (!enough) {
            coinDataResult.setEnough(false);
            return coinDataResult;
        }
        return coinDataResult;
    }

    @Override
    public boolean isLocalAccount(byte[] address) {
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
    public List<Account> getLocalAccountList() {
        return localAccountList;
    }

    @Override
    public Result transfer(byte[] from, byte[] to, Na values, String password, String remark) {
        try {
            Result<Account> accountResult = accountService.getAccount(from);
            if (accountResult.isFailed()) {
                return accountResult;
            }
            Account account = accountResult.getData();

            Result passwordResult = accountService.validPassword(account, password);
            if (passwordResult.isFailed()) {
                return passwordResult;
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
            if (coinDataResult.isEnough()) {
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

            tx.verifyWithException();
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
            e.printStackTrace();
            return Result.getFailed(e.getMessage());
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    protected Result<Integer> saveConfirmedTransaction(Transaction tx, byte status) {
        if (!isLocalTransaction(tx)) {
            return Result.getFailed().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(status);
        List<byte[]> addresses = new ArrayList<>();
        byte[] addressesBytes = tx.getAddressFromSig();

        if (addressesBytes == null || addressesBytes.length == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }

        if (addressesBytes.length % Address.size() != 0) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR);
        }

        for (int i = 0; i < addressesBytes.length / Address.size(); i++) {
            byte[] tmpAddress = new byte[Address.size()];
            System.arraycopy(addressesBytes, i * Address.size(), tmpAddress, 0, Address.size());
            if (isLocalAccount(tmpAddress)) {
                addresses.add(tmpAddress);
            }
        }

        Result result = storageService.saveLocalTxInfo(txInfoPo, addresses);

        if (result.isFailed()) {
            return result;
        }
        result = storageService.saveLocalTx(tx);
        if (result.isFailed()) {
            storageService.deleteLocalTxInfo(txInfoPo);
        }
        return result;
    }


    protected List<Transaction> getLocalTransaction(List<Transaction> txs) {
        List<Transaction> resultTxs = new ArrayList<>();
        if (txs == null || txs.size() == 0) {
            return resultTxs;
        }
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

    protected boolean isLocalTransaction(Transaction tx) {
        if (tx == null) {
            return false;
        }
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

    public void reloadAccount() {
        localAccountList = accountService.getAccountList().getData();
    }

}

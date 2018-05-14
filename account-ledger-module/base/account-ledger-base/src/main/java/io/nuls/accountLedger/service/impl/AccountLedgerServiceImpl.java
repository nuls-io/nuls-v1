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

package io.nuls.accountLedger.service.impl;

import com.sun.org.apache.regexp.internal.RE;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.account.service.AccountService;
import io.nuls.accountLedger.constant.AccountLedgerErrorCode;
import io.nuls.accountLedger.model.TransactionInfo;
import io.nuls.accountLedger.storage.po.TransactionInfoPo;
import io.nuls.accountLedger.storage.service.AccountLedgerStorageService;
import io.nuls.accountLedger.util.CoinComparator;
import io.nuls.core.tools.BloomFilter.BloomFilter;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;

import io.nuls.accountLedger.service.AccountLedgerService;

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

    private static List<Account> localAccountList;

    @Override
    public Result<Integer> save(Transaction tx) {
        if (!isLocalTransaction(tx)) {
            return Result.getFailed().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        List<byte[]> addresses = new ArrayList<>();
        byte[] addressesBytes = tx.getAddress();

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

    @Override
    public Result<Integer> saveList(List<Transaction> txs) {
        List<Transaction> txListToSave = getLocalTransaction(txs);
        List<Transaction> savedTxList = new ArrayList<>();
        Result result;
        for (int i = 0; i < txListToSave.size(); i++) {
            result = save(txListToSave.get(i));
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
        List<Coin> coinList = storageService.getCoinBytes(address);
        Balance balance = new Balance();
        long usable = 0;
        long locked = 0;

        long currentTime = System.currentTimeMillis();
        //long bestHeight = NulsContext.getInstance().getBestHeight();
        long bestHeight = 1;

        for (Coin coin : coinList) {
            if (coin.getLockTime() < 0) {
                locked += coin.getNa().getValue();
            } else if (coin.getLockTime() == 0) {
                usable += coin.getNa().getValue();
            } else {
                if (coin.getLockTime() > NulsConstant.BlOCKHEIGHT_TIME_DIVIDE) {
                    if (coin.getLockTime() <= currentTime) {
                        usable += coin.getNa().getValue();
                    } else {
                        locked += coin.getNa().getValue();
                    }
                } else {
                    if (coin.getLockTime() <= bestHeight) {
                        usable += coin.getNa().getValue();
                    } else {
                        locked += coin.getNa().getValue();
                    }
                }
            }
        }

        balance.setUsable(Na.valueOf(usable));
        balance.setLocked(Na.valueOf(locked));
        balance.setBalance(balance.getUsable().add(balance.getLocked()));
        Result<Balance> result = new Result<>();
        result.setData(balance);
        return result;
    }

    @Override
    public List<Coin> getCoinData(byte[] address, Na amount) throws NulsException {
        List<Coin> coinList = storageService.getCoinBytes(address);
        if (coinList.isEmpty()) {
            return coinList;
        }
        Collections.sort(coinList, CoinComparator.getInstance());

        boolean enough = false;
        List<Coin> coins = new ArrayList<>();
        Na values = Na.ZERO;
        for (int i = 0; i < coinList.size(); i++) {
            coins.add(coinList.get(i));
            values = values.add(coinList.get(i).getNa());
            if (values.isGreaterOrEquals(values)) {
                enough = true;
                break;
            }
        }
        if (!enough) {
            coins = new ArrayList<>();
        }
        return coins;
    }

    protected boolean isLocalTransaction(Transaction tx) {
        return false;
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
            List<byte[]> addresses = tmpTx.getAllRelativeAddress();
            for (int j = 0; j < addresses.size(); i++) {
                if (isLocalAccount(addresses.get(i))) {
                    resultTxs.add(tmpTx);
                    continue;
                }
            }
        }

        return resultTxs;
    }

    public void reloadAccount() {
        localAccountList = accountService.getAccountList().getData();
    }

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
}

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

import io.nuls.account.model.Account;
import io.nuls.account.model.Balance;
import io.nuls.account.service.AccountService;
import io.nuls.accountLedger.model.TransactionInfo;
import io.nuls.accountLedger.storage.po.TransactionInfoPo;
import io.nuls.accountLedger.storage.service.AccountLedgerStorageService;
import io.nuls.core.tools.BloomFilter.BloomFilter;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;

import io.nuls.accountLedger.service.AccountLedgerService;

import java.util.ArrayList;
import java.util.List;

/**
 * author Facjas
 * date 2018/5/10.
 */
public class AccountLedgerServiceImpl implements AccountLedgerService {

    @Autowired
    private AccountLedgerStorageService storageService;

    @Autowired
    private AccountService accountService;

    @Override
    public Result<Integer> save(Transaction tx) {
        if (!isLocalTransaction(tx)) {
            return Result.getFailed().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        Result result = storageService.saveLocalTxInfo(txInfoPo);
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
            }
        }
        return Result.getSuccess().setData(txListToSave.size());
    }

    @Override
    public Result<Integer> rollback(Transaction tx) {
        return null;
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
    public Result<Balance> getBalance(byte[] addres) {
        return null;
    }

    @Override
    public List<Coin> getCoinData(byte[] addres, Na amount) {
        return null;
    }

    protected boolean isLocalTransaction(Transaction tx) {
        return false;
    }

    protected List<Transaction> getLocalTransaction(List<Transaction> txs) {
        List<Transaction> resultTxs = new ArrayList<>();
        if (txs == null || txs.size() == 0) {
            return resultTxs;
        }

        List<Account> localAddressList = accountService.getAccountList().getData();
        if (localAddressList == null || localAddressList.size() == 0) {
            return resultTxs;
        }

        BloomFilter accountFilter = new BloomFilter(localAddressList.size() * 100, 0.0001, txs.get(1).getTime());
        for (int i = 0; i < localAddressList.size(); i++) {
            accountFilter.insert(localAddressList.get(i).getAddress().getBase58Bytes());
        }
        Transaction tmpTx;
        for (int i = 0; i < txs.size(); i++) {
            tmpTx = txs.get(i);
            List<byte[]> addresses = tmpTx.getAllRelativeAddress();
            for (int j = 0; j < addresses.size(); i++) {
                if (accountFilter.contains(addresses.get(i))) {
                    resultTxs.add(tmpTx);
                    continue;
                }
            }
        }

        return resultTxs;
    }
}

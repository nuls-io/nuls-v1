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

package io.nuls.accountLedger.service;

import io.nuls.account.model.Balance;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;

import io.nuls.accountLedger.service.AccountLedgerService;

import java.util.List;

/**
 * author Facjas
 * date 2018/5/10.
 */
public class AccountLedgerServiceImpl implements AccountLedgerService{


    @Override
    public Result<Integer> save(Transaction tx){
        return null;
    }

    @Override
    public Result<Integer> saveList(List<Transaction> txs){
        return null;
    }

    @Override
    public Result<Integer> rollback(Transaction tx){
        return null;
    }

    @Override
    public Result<Integer> rollback(List<Transaction> txs){
        return null;
    }

    @Override
    public Result<Balance> getBalance(byte[] addres){
        return null;
    }

    @Override
    public List<Coin> getCoinData(byte[] addres, Na amount){
        return null;
    }
}

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

package io.nuls.account.service;

import io.nuls.account.model.Account;
import io.nuls.kernel.model.*;

import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/5/5
 */
public interface AccountTxService {

    Result<Transaction> saveAccountTx(Transaction tx);

    /**
     * 先检查是否跟自己相关，相关的情况才保存
     * @param tx
     * @return
     */
    Result<Transaction> checkAndSaveAccountTx(Transaction tx);

    Result removeAccountTx(NulsDigestData txHash);

    Result<Transaction> updateAccountTx(Transaction tx);

    Result<List<Transaction>> getUnconfirmAccountTxList(Account account);
    Result<List<Transaction>> getUnconfirmAccountTxList();

    Result<List<Coin>> getUseableCoinList(Account account,Na na);
}

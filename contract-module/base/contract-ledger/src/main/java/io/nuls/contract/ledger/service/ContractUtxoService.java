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
package io.nuls.contract.ledger.service;

import io.nuls.account.model.Balance;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;

import java.math.BigInteger;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/5
 */
public interface ContractUtxoService {

    /**
     * 两种交易
     *   第一种交易是普通地址转入合约地址
     *      -> 合约账本只处理tx中的toCoinData -> 保存UTXO到合约账本
     *   第二种交易是智能合约特殊转账交易，合约地址转出到普通地址
     *      -> 合约账本只处理tx中的fromCoinData -> 删除UTXO从合约账本中
     *
     * @param tx
     * @return
     */
    Result saveUtxoForContractAddress(Transaction tx);

    Result deleteUtxoOfTransaction(Transaction tx);

    Result<BigInteger> getBalance(byte[] address);
}

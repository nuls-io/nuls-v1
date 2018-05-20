/**
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
 */
package io.nuls.ledger.service;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.*;
import io.nuls.kernel.validate.ValidateResult;

import java.util.List;

/**
 * Created by ln on 2018/5/4.
 */
public interface LedgerService {

    /**
     * Save transactions, automatically handle transactional coin data
     *
     * 保存交易，自动处理交易自带的coindata
     * @param tx
     * @return boolean
     */
    Result saveTx(Transaction tx) throws NulsException;

    /**
     * Roll back transactions while rolling back coindata data
     *
     * 回滚交易，同时回滚coindata数据
     * @param tx
     * @return boolean
     */
    Result rollbackTx(Transaction tx) throws NulsException;

    /**
     * get a transaction
     *
     * 获取一笔交易
     * @param hash
     * @return
     */
    Transaction getTx(NulsDigestData hash);

    /**
     * Verify that a coindata is valid, the first verification owner is legal (whether it can be used), the second verification amount is correct (output can not be greater than the input)
     * Check whether every from one in the coinData exists in txList database, or if not, is to continue to check the from of the existence of the deal and if it exists, represents a double spend, does not exist, is the orphan transactions, finally throw an exception
     *
     * 验证一笔coindata是否合法，验证拥有者是否合法（是否可动用），验证金额是否正确（输出不能大于输入）
     * 检查coinData里的每一笔from是否存在于txList或者数据库中，如果不存在，则继续检查from中那笔交易是否存在，如果存在，则代表双花，不存在，则是孤儿交易，最后抛出异常
     * @param transaction
     * @param txList
     * @return
     */
    ValidateResult verifyCoinData(Transaction transaction, List<Transaction> txList);

    /**
     * Verify that the from is repeated, and if repeated, it represents a double spend and throws an exception.
     *
     * 验证from是否重复，如果重复，则代表双花，并抛出异常
     * @param block
     * @return
     */
    ValidateResult<List<Transaction>> verifyDoubleSpend(Block block);

    /**
     * Verify that the from is repeated, and if repeated, it represents a double spend and throws an exception.
     *
     * 验证from是否重复，如果重复，则代表双花，并抛出异常
     * @param txList
     * @return
     */
    ValidateResult<List<Transaction>> verifyDoubleSpend(List<Transaction> txList);

    /**
     * Unlock the coindata of a transaction. When certain business scenarios require a certain amount of funds to be locked, an action is unlocked at some point in the future, the method is called, and the lock state changes to the spent state.
     * The specific operation is to determine whether the from data in the coindata is -1, if it is not then return failure, if it is deleted, and then write the new to the input pool does not spend
     *
     * 解锁一笔交易的coindata，当某些业务场景需要锁定一定数量的资金，在未来某个时刻某个动作解锁时，调用该方法，由锁定状态变为已花费状态
     * 具体操作的是先判断coindata里面的from是否为-1，如果不是则返回失败，如果是则删除，然后把新的to写入未花费的输入池
     * @param tx
     * @return boolean
     */
    Result unlockTxCoinData(Transaction tx,long newockTime) throws NulsException;

    /**
     * rollback unlockTxCoinData
     *
     * 回滚unlockTxCoinData
     * @param tx
     * @return boolean
     */
    Result rollbackUnlockTxCoinData(Transaction tx) throws NulsException;
}

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

import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;

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
    boolean saveTx(Transaction tx);

    /**
     * Roll back transactions while rolling back coindata data
     *
     * 回滚交易，同时回滚coindata数据
     * @param tx
     * @return boolean
     */
    boolean rollbackTx(Transaction tx);

    /**
     * get a transaction
     *
     * 获取一笔交易
     * @param hash
     * @return
     */
    Transaction getTx(NulsDigestData hash);

    /**
     * Verify that a coindata is valid, verify 2 points, the first verification owner is legal (whether it can be used), the second verification amount is correct (output can not be greater than the input)
     *
     * 验证一笔coindata是否合法，验证2点，第一验证拥有者是否合法（是否可动用），第二验证金额是否正确（输出不能大于输入）
     * @param coinData
     * @return
     */
    boolean verifyCoinData(CoinData coinData);

    /**
     * To unlock the coindata of a transaction, when certain business scenarios need to lock a certain amount of funds, and an action is unlocked at a certain time in the future, the method is called, and the locked state is changed to the available state. The specific operation is to set lockTime from - 1 to 0
     *
     * 解锁一笔交易的coindata，当某些业务场景需要锁定一定数量的资金，在未来某个时刻某个动作解锁时，调用该方法，由锁定状态变为可用状态，具体操作的是把lockTime由-1改为0
     * @param tx
     * @return boolean
     */
    boolean unlockTxCoinData(Transaction tx);
}

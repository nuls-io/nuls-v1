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
package io.nuls.protocol.intf;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.validate.ValidateResult;

import java.util.List;

/**
 * 交易处理器，每个交易需要实现自己的交易处理器，处理交易本身的业务，交易不同的生命周期会调用不同的业务方法
 * <p>
 * Transaction processors, each transaction needs to implement its own transaction processor,
 * deal with the business of the transaction itself, and different business methods are invoked by different life cycles.
 *
 * @author Niels
 * @date 2017/12/14
 */
public interface TransactionProcessor<T extends Transaction> {

    /**
     * 交易回滚时调用该方法
     * This method is called when the transaction rolls back.
     *
     * @param tx            要回滚的交易，The transaction to roll back.
     * @param secondaryData 辅助数据，视业务需要传递，Secondary data, depending on the business needs to be passed.
     */
    void onRollback(T tx, Object secondaryData) throws NulsException;

    /**
     * 交易存储时调用该方法
     * This method is called when the transaction save.
     *
     * @param tx            要保存的交易，The transaction to save;
     * @param secondaryData 辅助数据，视业务需要传递，Secondary data, depending on the business needs to be passed.
     */
    void onCommit(T tx, Object secondaryData) throws NulsException;

    /**
     * 冲突检测，检测如果传入的交易列表中有和该交易相冲突的交易，则返回失败，写明失败原因
     * <p>
     * Conflict detection. If a transaction in the incoming transaction list is in conflict with the transaction,
     * the failure is returned, indicating the cause of failure.
     *
     * @param tx     需要进行检测的交易，The transactions that need to be tested.
     * @param txList 交易列表
     */
    ValidateResult conflictDetect(T tx, List<Transaction> txList);


}

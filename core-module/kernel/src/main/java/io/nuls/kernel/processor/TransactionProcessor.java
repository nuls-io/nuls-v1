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
package io.nuls.kernel.processor;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.validate.ValidateResult;

import java.util.List;

/**
 * 交易处理器，每个交易需要实现自己的交易处理器，处理交易本身的业务，交易不同的生命周期会调用不同的业务方法
 * 实现该接口的类，需要添加{@link io.nuls.kernel.lite.annotation.Service} 注解
 * <p>
 * Transaction processors, each transaction needs to implement its own transaction processor,
 * deal with the business of the transaction itself, and different business methods are invoked by different life cycles.
 * Implement this interface class, you need to add {@link io.nuls.kernel.lite.annotation.Service} annotation
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
    Result onRollback(T tx, Object secondaryData);

    /**
     * 交易存储时调用该方法
     * This method is called when the transaction save.
     *
     * @param tx            要保存的交易，The transaction to save;
     * @param secondaryData 辅助数据，视业务需要传递，Secondary data, depending on the business needs to be passed.
     */
    Result onCommit(T tx, Object secondaryData);

    /**
     * 冲突检测，检测如果传入的交易列表中有相冲突的交易，则返回失败，写明失败原因及所有的应该舍弃的交易列表
     * 本方法不检查双花冲突，双花由账本接口实现
     * <p>
     * Conflict detection, which detects conflicting transactions in the incoming transaction list, returns failure,
     * indicating the cause of failure and all the list of trades that should be discarded.
     * This method does not check the double flower conflict, the double flower is realized by the accounting interface.
     *
     * @param txList 需要检查的交易列表/A list of transactions to be checked.
     * @return 操作结果：成功则返回successResult，失败时，data中返回丢弃列表，msg中返回冲突原因
     * Operation result: success returns successResult. When failure, data returns the discard list, and MSG returns the cause of conflict.
     */
    ValidateResult conflictDetect(List<Transaction> txList);
}

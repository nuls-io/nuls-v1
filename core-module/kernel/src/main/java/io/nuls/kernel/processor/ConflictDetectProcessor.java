/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.validate.ValidateResult;

import java.util.List;

/**
 * @author: Niels Wang
 */
public interface ConflictDetectProcessor {

    /**
     * 冲突检测，检测如果传入的交易列表中有相冲突的交易，则返回失败，写明失败原因及所有的应该舍弃的交易
     * 本方法不检查双花冲突，双花由账本接口实现
     * <p>
     * Conflict detection, which detects conflicting transactions in the incoming transaction list, returns failure,
     * indicating the cause of failure and the transaction that should be discarded.
     * This method does not check the double flower conflict, the double flower is realized by the accounting interface.
     *
     * @param txList 需要检查的交易列表/A list of transactions to be checked.
     * @return 操作结果：成功则返回successResult，失败时，data中返回丢弃列表，msg中返回冲突原因
     * Operation result: success returns successResult. When failure, data returns the discard list, and MSG returns the cause of conflict.
     */
    ValidateResult conflictDetect(List<Transaction> txList);
}

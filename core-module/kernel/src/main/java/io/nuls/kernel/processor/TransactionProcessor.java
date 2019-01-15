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

import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;

/**
 * 交易处理器，每个交易需要实现自己的交易处理器，处理交易本身的业务，交易不同的生命周期会调用不同的业务方法
 * 实现该接口的类，需要添加{@link io.nuls.kernel.lite.annotation.Service} 注解
 * Transaction processors, each transaction needs to implement its own transaction cmd,
 * deal with the business of the transaction itself, and different business methods are invoked by different life cycles.
 * Implement this interface class, you need to add {@link io.nuls.kernel.lite.annotation.Service} annotation
 *
 * @author Niels
 */
public interface TransactionProcessor<T extends Transaction> extends ConflictDetectProcessor {

    //    /**
//     * 交易回滚时调用该方法
//     * This method is called when the transaction rolls back.
//     *
//     * @param tx            要回滚的交易，The transaction to roll back.
//     * @param secondaryData 辅助数据，视业务需要传递，Secondary data, depending on the business needs to be passed.
//     */
    Result onRollback(T tx, Object secondaryData);

    //    /**
//     * 交易存储时调用该方法
//     * This method is called when the transaction save.
//     *
//     * @param tx            要保存的交易，The transaction to save;
//     * @param secondaryData 辅助数据，视业务需要传递，Secondary data, depending on the business needs to be passed.
//     */
    Result onCommit(T tx, Object secondaryData);


}

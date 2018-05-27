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

package io.nuls.protocol.base.service;

import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.utils.TransactionManager;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.message.TransactionMessage;
import io.nuls.protocol.service.TransactionService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/5/8
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private MessageBusService messageBusService;

    @Autowired
    private LedgerService ledgerService;

    /**
     * 确认交易时调用的方法，对交易相关的业务进行提交操作
     * Identify the method that is invoked during the transaction and submit the transaction related business.
     *
     * @param tx            操作的交易/The transaction of the operation
     * @param secondaryData 辅助数据（可以为空）/Secondary data (available for null)
     * @return 操作结果/operating results
     */
    @Override
    public Result commitTx(Transaction tx, Object secondaryData) {
        List<TransactionProcessor> processorList = TransactionManager.getProcessorList(tx.getClass());
        List<TransactionProcessor> commitedProcessorList = new ArrayList<>();
        for (TransactionProcessor processor : processorList) {
            Result result = processor.onCommit(tx, secondaryData);
            if (result.isSuccess()) {
                commitedProcessorList.add(processor);
            } else {
                for (int i = commitedProcessorList.size() - 1; i >= 0; i--) {
                    TransactionProcessor processor1 = commitedProcessorList.get(i);
                    processor1.onRollback(tx, secondaryData);
                }
                return result;
            }
        }
        return Result.getSuccess();
    }

    /**
     * 回滚交易时调用的方法，对交易相关的业务进行回退操作
     * The method invoked when the transaction is rolled back and the transaction related business is returned.
     *
     * @param tx            操作的交易/The transaction of the operation
     * @param secondaryData 辅助数据（可以为空）/Secondary data (available for null)
     * @return 操作结果/operating results
     */
    @Override
    public Result rollbackTx(Transaction tx, Object secondaryData) {
        List<TransactionProcessor> processorList = TransactionManager.getProcessorList(tx.getClass());
        List<TransactionProcessor> rollbackedList = new ArrayList<>();
        for (TransactionProcessor processor : processorList) {
            Result result = processor.onRollback(tx, secondaryData);
            if (result.isSuccess()) {
                rollbackedList.add(processor);
            } else {
                for (int i = rollbackedList.size() - 1; i >= 0; i--) {
                    TransactionProcessor processor1 = rollbackedList.get(i);
                    processor1.onCommit(tx, secondaryData);
                }
                return result;
            }
        }
        return Result.getSuccess();
    }

    /**
     * 转发交易给连接的其他对等节点，允许一个列外（不转发给它）
     * Forward Transaction to other peers of the connection, allowing one column (not forward to it)
     *
     * @param tx          完整交易/the whole transaction
     * @param excludeNode 需要排除的节点，一般是因为从该节点处接收的本交易/The nodes that need to be excluded are generally due to the transaction received from the node.
     * @return 转发结果/forward results
     */
    @Override
    public Result forwardTx(Transaction tx, Node excludeNode) {
        TransactionMessage message = new TransactionMessage();
        message.setMsgBody(tx);
        return messageBusService.broadcastHashAndCache(message, excludeNode, true);
    }

    /**
     * 广播交易给连接的其他对等节点
     * The broadcast transaction gives the connection to other peers.
     *
     * @param tx 完整交易/the whole transaction
     * @return 广播结果/Broadcast the results
     */
    @Override
    public Result broadcastTx(Transaction tx) {
        TransactionMessage message = new TransactionMessage();
        message.setMsgBody(tx);
        return messageBusService.broadcastAndCache(message, null, true);
    }

    /**
     * 冲突检测，检测如果传入的交易列表中有相冲突的交易，则返回失败，写明失败原因及所有的应该舍弃的交易列表
     * <p>
     * Conflict detection, which detects conflicting transactions in the incoming transaction list, returns failure,
     * indicating the cause of failure and all the list of trades that should be discarded.
     *
     * @param txList 需要检查的交易列表/A list of transactions to be checked.
     * @return 操作结果：成功则返回successResult，失败时，data中返回丢弃列表，msg中返回冲突原因
     * Operation result: success returns successResult. When failure, data returns the discard list, and MSG returns the cause of conflict.
     */
    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        if (null == txList || txList.size() <= 1) {
            return ValidateResult.getSuccessResult();
        }
//        ValidateResult result = ledgerService.verifyDoubleSpend(txList);
//        if (result.isFailed()) {
//            return result;
//        }
        List<Transaction> newTxList = new ArrayList<>();
        for (Transaction tx : txList) {
            if (tx.getType() == ProtocolConstant.TX_TYPE_COINBASE || tx.getType() == ProtocolConstant.TX_TYPE_TRANSFER) {
                continue;
            }
            newTxList.add(tx);
        }
        List<TransactionProcessor> processorList = TransactionManager.getAllProcessorList();
        ValidateResult result = ValidateResult.getSuccessResult();
        for (TransactionProcessor processor : processorList) {
            result = processor.conflictDetect(newTxList);
            if (result.isFailed()) {
                break;
            }
        }
        return result;
    }
}

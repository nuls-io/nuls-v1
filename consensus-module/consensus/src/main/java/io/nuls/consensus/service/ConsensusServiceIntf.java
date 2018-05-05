/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.service;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.network.entity.Node;

import java.util.List;

/**
 * Created by ln on 2018/5/4.
 */
public interface ConsensusServiceIntf {

    /**
     * receive a new transaction, add in memory pool after verify success
     * @param tx
     * @return boolean
     */
    boolean newTx(Transaction<? extends BaseNulsData> tx);

    /**
     * receive block from other peers
     * @param block
     * @return boolean
     */
    boolean newBlock(Block block);

    /**
     * receive block from other peers
     * @param block
     * @param node
     * @return boolean
     */
    boolean newBlock(Block block, Node node);

    /**
     * synchronous block from other peers
     * @param block
     * @return boolean
     */
    boolean addBlock(Block block);

    /**
     * Roll back the latest block and roll back the status of the chain in the consensus service memory
     *
     * 回滚最新区块，同时回滚共识服务内存中链的状态
     * @return boolean
     */
    boolean rollbackBlock(Block block) throws NulsException;

    /**
     * Gets a transaction from the memory pool, returns and removes it from the memory pool, or null if it does not exist
     * The method is invoked by the service department, and the application scenario is to assemble a complete block from a memory acquisition transaction when a new block is generated.
     *
     * 从内存池中获取一笔交易，返回并从内存池中删除，如果不存在，则返回null
     * 本方法供部调用，应用场景为接收到新区块产生时从内存获取交易组装完整的区块
     *
     * @return Transaction
     */
    //todo 内部
    Transaction getAndRemoveOfMemoryTxs(NulsDigestData hash);

    /**
     * Get a specified transaction from the memory pool, or null if none
     *
     * 从内存池中获取一条指定的交易，如果没有则返回null
     * @param hash
     * @return Transaction
     */
    //todo 内部
    Transaction getTxFromMemory(NulsDigestData hash);

    /**
     * Get all the transactions in the memory pool
     *
     * 获取内存池里面的所有交易
     * @return List<BaseNulsData>
     */
    List<BaseNulsData> getMemoryTxs();

    /**
     * Reset consensus module, restart, load memory data, reinitialize all states
     *
     * 重置共识模块，会重新启动，加载内存数据，重新初始化所有状态
     * @return boolean
     */
    //todo 内部
    boolean reset();

}
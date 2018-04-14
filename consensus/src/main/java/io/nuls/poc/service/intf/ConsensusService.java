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
 */
package io.nuls.poc.service.intf;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.network.entity.Node;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.core.chain.entity.BaseNulsData;

import java.util.List;

/**
 * @author ln
 * @date 2018/4/12
 */
public interface ConsensusService {

    boolean startup();

    boolean restart();

    boolean shutdown();

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

    ConsensusStatus getConsensusStatus();

    List<BaseNulsData> getMemoryTxs();

    /**
     * Gets a transaction from the memory pool, returns and removes it from the memory pool, or null if it does not exist
     * The method is invoked by the service department, and the application scenario is to assemble a complete block from a memory acquisition transaction when a new block is generated.
     *
     * 从内存池中获取一笔交易，返回并从内存池中删除，如果不存在，则返回null
     * 本方法供部调用，应用场景为接收到新区块产生时从内存获取交易组装完整的区块
     *
     * @return BaseNulsData
     */
    BaseNulsData getAndRemoveOfMemoryTxs(String hash);
}

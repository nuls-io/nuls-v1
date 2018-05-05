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

package io.nuls.consensus.poc.service;

import io.nuls.consensus.poc.cache.BlockMemoryPool;
import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.service.ConsensusServiceIntf;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.network.entity.Node;

import java.util.List;

/**
 * Created by ln on 2018/5/5.
 */
@Service
public class ConsensusPocService implements ConsensusServiceIntf {

    private TxMemoryPool txMemoryPool = new TxMemoryPool();
    private BlockMemoryPool blockMemoryPool = new BlockMemoryPool();

    @Override
    public Result newTx(Transaction<? extends BaseNulsData> tx) {
        boolean success = txMemoryPool.add(tx);
        return new Result(success, null);
    }

    @Override
    public Result newBlock(Block block) {
        return newBlock(block, null);
    }

    @Override
    public Result newBlock(Block block, Node node) {
        BlockContainer blockContainer = new BlockContainer(block, node, BlockContainerStatus.RECEIVED);
        boolean success = blockMemoryPool.put(blockContainer);
        return new Result(success, null);
    }

    @Override
    public Result addBlock(Block block) {
        BlockContainer blockContainer = new BlockContainer(block, BlockContainerStatus.DOWNLOADING);
        boolean success = blockMemoryPool.put(blockContainer);
        return new Result(success, null);
    }

    @Override
    public Result rollbackBlock(Block block) throws NulsException {
        return null;
    }

    @Override
    public List<Transaction> getMemoryTxs() {
        return txMemoryPool.getAll();
    }

    @Override
    public Result reset() {
        //TODO
        return null;
    }
}
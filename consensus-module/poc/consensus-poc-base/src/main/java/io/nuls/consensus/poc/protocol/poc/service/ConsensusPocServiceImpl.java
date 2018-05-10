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

package io.nuls.consensus.poc.protocol.poc.service;

import io.nuls.consensus.poc.protocol.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.protocol.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.protocol.poc.container.BlockContainer;
import io.nuls.consensus.poc.protocol.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.protocol.poc.locker.Lockers;
import io.nuls.consensus.poc.protocol.poc.provider.BlockQueueProvider;
import io.nuls.consensus.poc.protocol.poc.scheduler.ConsensusScheduler;
import io.nuls.consensus.poc.protocol.service.ConsensusServiceIntf;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.network.entity.Node;
import io.nuls.protocol.service.BlockService;

import java.util.List;

/**
 * Created by ln on 2018/5/5.
 */
@Service
public class ConsensusPocServiceImpl implements ConsensusServiceIntf {

    private TxMemoryPool txMemoryPool = TxMemoryPool.getInstance();
    private BlockQueueProvider blockQueueProvider = BlockQueueProvider.getInstance();

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    @Override
    public Result newTx(Transaction<? extends BaseNulsData> tx) {
        // Validate the transaction. If the verification is passed, it will be put into the transaction memory pool.
        // If the verification is an isolated transaction, it will be put in the isolated transaction pool. Other failures will directly discard the transaction.
        // 验证交易，验证通过则放入交易内存池中，如果验证到是孤立交易，则放入孤立交易池里，其它失败情况则直接丢弃交易
        ValidateResult verifyResult = tx.verify();
        boolean success = false;
        if(verifyResult.isSuccess()) {
            success = txMemoryPool.add(tx, false);
        } else if(verifyResult.isFailed() && TransactionErrorCode.ORPHAN_TX == verifyResult.getErrorCode()) {
            success = txMemoryPool.add(tx, true);
        }
        return new Result(success, null);
    }

    @Override
    public Result newBlock(Block block) {
        return newBlock(block, null);
    }

    @Override
    public Result newBlock(Block block, Node node) {
        BlockContainer blockContainer = new BlockContainer(block, node, BlockContainerStatus.RECEIVED);
        boolean success = blockQueueProvider.put(blockContainer);
        return new Result(success, null);
    }

    @Override
    public Result addBlock(Block block) {
        BlockContainer blockContainer = new BlockContainer(block, BlockContainerStatus.DOWNLOADING);
        boolean success = blockQueueProvider.put(blockContainer);
        return new Result(success, null);
    }

    @Override
    public Result rollbackBlock(Block block) throws NulsException {

        boolean success;
        Lockers.CHAIN_LOCK.lock();
        try {
            success = PocConsensusContext.getChainManager().getMasterChain().rollback(block);
        } finally {
            Lockers.CHAIN_LOCK.unlock();
        }
        if(success) {
            success = blockService.rollbackBlock(block).isSuccess();
            if(!success) {
                PocConsensusContext.getChainManager().getMasterChain().addBlock(block);
            }
        }
        return new Result(success, null);
    }

    @Override
    public List<Transaction> getMemoryTxs() {
        return txMemoryPool.getAll();
    }

    @Override
    public Result reset() {
        boolean success = ConsensusScheduler.getInstance().restart();
        return new Result(success, null);
    }
}
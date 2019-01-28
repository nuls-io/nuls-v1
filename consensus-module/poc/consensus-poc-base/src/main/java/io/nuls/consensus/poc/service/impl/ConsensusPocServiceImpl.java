/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.consensus.poc.service.impl;

import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.locker.Lockers;
import io.nuls.consensus.poc.process.NulsProtocolProcess;
import io.nuls.consensus.poc.process.RewardStatisticsProcess;
import io.nuls.consensus.poc.provider.BlockQueueProvider;
import io.nuls.consensus.poc.scheduler.ConsensusScheduler;
import io.nuls.consensus.poc.storage.service.RandomSeedsStorageService;
import io.nuls.consensus.poc.storage.service.TransactionCacheStorageService;
import io.nuls.consensus.poc.storage.service.TransactionQueueStorageService;
import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.ledger.service.LedgerService;
import io.nuls.network.model.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.DownloadService;

import java.util.List;

/**
 * @author ln
 */
@Service
public class ConsensusPocServiceImpl implements ConsensusService {

    private TxMemoryPool txMemoryPool = TxMemoryPool.getInstance();

    private BlockQueueProvider blockQueueProvider = BlockQueueProvider.getInstance();

    private NulsProtocolProcess nulsProtocolProcess = NulsProtocolProcess.getInstance();

    @Autowired
    private BlockService blockService;
    @Autowired
    private LedgerService ledgerService;
    @Autowired
    private TransactionQueueStorageService transactionQueueStorageService;
    @Autowired
    private TransactionCacheStorageService transactionCacheStorageService;
    @Autowired
    private RandomSeedsStorageService randomSeedsStorageService;

    @Override
    public Result newTx(Transaction<? extends BaseNulsData> tx) {
        // Validate the transaction. If the verification is passed, it will be put into the transaction memory pool.
        // If the verification is an isolated transaction, it will be put in the isolated transaction pool. Other failures will directly discard the transaction.
        // 验证交易，验证通过则放入交易内存池中，如果验证到是孤立交易，则放入孤立交易池里，其它失败情况则直接丢弃交易
//        ValidateResult verifyResult = tx.verify();
//        boolean success = false;
//        if (verifyResult.isSuccess()) {
//            success = txMemoryPool.add(new TxContainer(tx), false);
//        } else if (verifyResult.isFailed() && TransactionErrorCode.ORPHAN_TX == verifyResult.getErrorCode()) {
//            success = txMemoryPool.add(new TxContainer(tx), true);
//        }

//        boolean success = txMemoryPool.add(new TxContainer(tx), false);
        boolean success = transactionQueueStorageService.putTx(tx);
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
        if (success) {
            success = blockService.rollbackBlock(block).isSuccess();
            if (!success) {
                PocConsensusContext.getChainManager().getMasterChain().addBlock(block);
            } else {
                //回滚版本更新统计数据
                nulsProtocolProcess.processProtocolRollback(block.getHeader());
                RewardStatisticsProcess.rollbackBlock(block);
                NulsContext.getInstance().setBestBlock(PocConsensusContext.getChainManager().getMasterChain().getBestBlock());
                randomSeedsStorageService.deleteRandomSeed(block.getHeader().getHeight());
            }
        }
        return new Result(success, null);
    }

    @Override
    public List<Transaction> getMemoryTxs() {
        return txMemoryPool.getAll();
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        Transaction tx = transactionCacheStorageService.getTx(hash);
        if (tx == null) {
            tx = ledgerService.getTx(hash);
        }
        return tx;
    }

    /**
     * Reset consensus module, restart, load memory data, reinitialize all states
     * <p>
     * 重置共识模块，会重新启动，加载内存数据，重新初始化所有状态
     *
     * @return Result
     */
    public Result reset() {
        Log.warn("Consensus restart...");
        boolean success = ConsensusScheduler.getInstance().restart();
        if (success) {
            NulsContext.getServiceBean(NetworkService.class).reset();
            NulsContext.getServiceBean(DownloadService.class).reset();
        }
        return new Result(success, null);
    }
}
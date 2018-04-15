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
package io.nuls.consensus.poc.service.impl;

import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.context.ConsensusContext;
import io.nuls.consensus.poc.entity.Agent;
import io.nuls.consensus.poc.provider.ConsensusSystemProvider;
import io.nuls.consensus.poc.provider.QueueProvider;
import io.nuls.consensus.poc.service.PocConsensusService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.network.entity.Node;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.consensus.poc.scheduler.MainControlScheduler;

import java.util.*;

/**
 * Created by ln on 2018/4/13.
 */
public class PocConsensusServiceImpl implements PocConsensusService {

    private QueueProvider blockQueueProvider;
    private QueueProvider txQueueProvider;

    private MainControlScheduler mainControlScheduler = MainControlScheduler.getInstance();

    @Override
    public boolean startup() {
        return mainControlScheduler.start();
    }

    @Override
    public boolean restart() {
        return mainControlScheduler.restart();
    }

    @Override
    public boolean shutdown() {
        return mainControlScheduler.stop();
    }

    @Override
    public boolean newTx(Transaction<? extends BaseNulsData> tx) {
        return txQueueProvider.put(tx, true);
    }

    @Override
    public boolean newBlock(Block block) {
        return blockQueueProvider.put(block, true);
    }

    @Override
    public boolean newBlock(Block block, Node node) {
        BlockContainer blockContainer = new BlockContainer();
        blockContainer.setBlock(block);
        blockContainer.setNode(node);
        return blockQueueProvider.put(blockContainer, true);
    }

    @Override
    public boolean addBlock(Block block) {
        return blockQueueProvider.put(block, false);
    }

    @Override
    public ConsensusStatus getConsensusStatus() {
        return ConsensusSystemProvider.getConsensusStatus();
    }

    @Override
    public List<BaseNulsData> getMemoryTxs() {
        List<BaseNulsData> list = new ArrayList<>();

        TxMemoryPool memoryPool = mainControlScheduler.getTxMemoryPool();

        list.addAll(memoryPool.getAll());
        list.addAll(memoryPool.getAll());

        return list;
    }

    @Override
    public BaseNulsData getAndRemoveOfMemoryTxs(String hash) {
        return mainControlScheduler.getTxMemoryPool().getAndRemove(hash);
    }

    @Override
    public List<Agent> getAgentList() {
        //TODO
        return null;
    }

    @Override
    public void addProvider(QueueProvider blockQueueProvider, QueueProvider txQueueProvider) {
        this.blockQueueProvider = blockQueueProvider;
        this.txQueueProvider = txQueueProvider;
    }
}

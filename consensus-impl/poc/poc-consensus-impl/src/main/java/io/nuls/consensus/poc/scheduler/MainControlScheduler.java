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
package io.nuls.consensus.poc.scheduler;

import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.manager.CacheManager;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.manager.RoundManager;
import io.nuls.consensus.poc.process.*;
import io.nuls.consensus.poc.protocol.locker.Lockers;
import io.nuls.consensus.poc.provider.BlockQueueProvider;
import io.nuls.consensus.poc.provider.ConsensusSystemProvider;
import io.nuls.consensus.poc.provider.IsolatedBlocksProvider;
import io.nuls.consensus.poc.provider.TxQueueProvider;
import io.nuls.consensus.poc.service.PocConsensusService;
import io.nuls.consensus.poc.task.*;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.thread.manager.NulsThreadFactory;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.protocol.context.NulsContext;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ln on 2018/4/13.
 */
public class MainControlScheduler {

    private static MainControlScheduler INSTANCE = new MainControlScheduler();

    private ScheduledThreadPoolExecutor threadPool;

    private BlockQueueProvider blockQueueProvider;
    private TxQueueProvider txQueueProvider;
    private ChainManager chainManager;
    private RoundManager roundManager;
    private CacheManager cacheManager;
    private TxMemoryPool txMemoryPool;

    private MainControlScheduler() {
    }

    public static MainControlScheduler getInstance() {
        return INSTANCE;
    }

    public boolean start() {

        ConsensusSystemProvider.setConsensusStatus(ConsensusStatus.INITING);

        //Create Queue Provider
        //创建队列提供器
        blockQueueProvider = new BlockQueueProvider();
        txQueueProvider = new TxQueueProvider();

        //Register Queue Provider into Service
        //把队列提供器注册进服务里面
        PocConsensusService pocConsensusService = NulsContext.getServiceBean(PocConsensusService.class);
        pocConsensusService.addProvider(blockQueueProvider, txQueueProvider);

        threadPool = TaskManager.createScheduledThreadPool(5,
                new NulsThreadFactory(NulsConstant.MODULE_ID_CONSENSUS, "consensus-poll-control"));

        chainManager = new ChainManager();
        roundManager = new RoundManager(chainManager);
        cacheManager = new CacheManager(chainManager);
        txMemoryPool = new TxMemoryPool();


        IsolatedBlocksProvider isolatedBlocksProvider = new IsolatedBlocksProvider();
        IsolatedBlocksProcess isolatedBlocksProcess = new IsolatedBlocksProcess(chainManager);
        threadPool.scheduleAtFixedRate(new IsolatedBlocksProcessTask(isolatedBlocksProcess, isolatedBlocksProvider), 1000L,1000L, TimeUnit.MILLISECONDS);

        BlockProcess blockProcess = new BlockProcess(chainManager, isolatedBlocksProvider);
        isolatedBlocksProcess.setBlockProcess(blockProcess);
        threadPool.scheduleAtFixedRate(new BlockProcessTask(blockProcess, blockQueueProvider), 1000L,100L, TimeUnit.MILLISECONDS);

        TxProcess txProcess = new TxProcess(txMemoryPool);
        threadPool.scheduleAtFixedRate(new TxProcessTask(txProcess, txQueueProvider), 1000L,500L, TimeUnit.MILLISECONDS);

        ChainProcess chainProcess = new ChainProcess(chainManager);
        threadPool.scheduleAtFixedRate(new ChainProcessTask(chainProcess), 1000L,500L, TimeUnit.MILLISECONDS);

        ConsensusProcess consensusProcess = new ConsensusProcess(chainManager, roundManager, txMemoryPool, blockProcess);
        threadPool.scheduleAtFixedRate(new ConsensusProcessTask(consensusProcess), 1000L,1000L, TimeUnit.MILLISECONDS);

        initDatas();

        return true;
    }

    public boolean restart() {

        clear();
        initDatas();

        return true;
    }

    public boolean stop() {

        clear();
        threadPool.shutdown();

        return true;
    }

    private void initDatas() {
        Lockers.OUTER_LOCK.lock();
        try {

            ConsensusSystemProvider.setConsensusStatus(ConsensusStatus.LOADING_CACHE);

            cacheManager.load();

        } catch (Exception e) {
            //TODO 缓存加载失败的处理
            e.printStackTrace();
            Log.error(e);
        } finally {
            ConsensusSystemProvider.setConsensusStatus(ConsensusStatus.WAIT_START);
            Lockers.OUTER_LOCK.unlock();
        }
    }

    private void clear() {
        Lockers.OUTER_LOCK.lock();
        try {
            cacheManager.clear();
            txQueueProvider.clear();
            blockQueueProvider.clear();
        } finally {
            Lockers.OUTER_LOCK.unlock();
        }
    }

    public TxMemoryPool getTxMemoryPool() {
        return txMemoryPool;
    }
}

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

package io.nuls.consensus.poc.scheduler;

import io.nuls.consensus.poc.constant.ConsensusStatus;
import io.nuls.consensus.poc.context.ConsensusStatusContext;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.manager.CacheManager;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.process.*;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.provider.OrphanBlockProvider;
import io.nuls.consensus.poc.task.*;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.protocol.base.version.NulsVersionManager;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.service.BlockService;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ln
 */
public class ConsensusScheduler {

    private static ConsensusScheduler INSTANCE = new ConsensusScheduler();

    private ScheduledThreadPoolExecutor threadPool;

    private OrphanBlockProcess orphanBlockProcess;

    private CacheManager cacheManager;

    private ConsensusScheduler() {
    }

    public static ConsensusScheduler getInstance() {
        return INSTANCE;
    }

    public boolean start() {

        ChainManager chainManager = new ChainManager();
        OrphanBlockProvider orphanBlockProvider = new OrphanBlockProvider();

        PocConsensusContext.setChainManager(chainManager);

        cacheManager = new CacheManager(chainManager);
        try {
            initDatas();
        } catch (Exception e) {
            Log.warn(e.getMessage());
        }

        //创建定时任务管理器
        threadPool = TaskManager.createScheduledThreadPool(6,
                new NulsThreadFactory(ConsensusConstant.MODULE_ID_CONSENSUS, "consensus-poll-control"));

        //区块验证定时任务
        BlockProcess blockProcess = new BlockProcess(chainManager, orphanBlockProvider);
        threadPool.scheduleAtFixedRate(new BlockProcessTask(blockProcess), 1000L, 300L, TimeUnit.MILLISECONDS);

        //区块分叉处理任务
        ForkChainProcess forkChainProcess = new ForkChainProcess(chainManager);
        threadPool.scheduleAtFixedRate(new ForkChainProcessTask(forkChainProcess), 1000L, 1000L, TimeUnit.MILLISECONDS);

        //共识任务，打包区块
        ConsensusProcess consensusProcess = new ConsensusProcess(chainManager);
        threadPool.scheduleAtFixedRate(new ConsensusProcessTask(consensusProcess), 1000L, 1000L, TimeUnit.MILLISECONDS);

        //孤儿块处理线程（守护线程）
        orphanBlockProcess = new OrphanBlockProcess(chainManager, orphanBlockProvider);
        orphanBlockProcess.start();

        //块计数器任务
        threadPool.scheduleAtFixedRate(new BlockMonitorProcessTask(new BlockMonitorProcess(chainManager)), 60, 60, TimeUnit.SECONDS);

        //报酬统计线程
        TaskManager.createAndRunThread(ConsensusConstant.MODULE_ID_CONSENSUS, "poc-reward-cache", new RewardStatisticsProcessTask(NulsContext.getServiceBean(RewardStatisticsProcess.class)));

        threadPool.scheduleAtFixedRate(new RewardCalculatorTask(NulsContext.getServiceBean(RewardStatisticsProcess.class)), ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND, ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND, TimeUnit.SECONDS);

        threadPool.scheduleAtFixedRate(new TxProcessTask(), 5, 1, TimeUnit.SECONDS);
        return true;
    }

    public boolean restart() {
        clear();
        initDatas();

        return true;
    }

    public boolean stop() {

        clear();

        orphanBlockProcess.stop();
        threadPool.shutdown();

        return true;
    }

    private void initDatas() {
        try {
            ConsensusStatusContext.setConsensusStatus(ConsensusStatus.LOADING_CACHE);
            cacheManager.load();

            ConsensusStatusContext.setConsensusStatus(ConsensusStatus.WAIT_RUNNING);
        } catch (Exception e) {
            throw new NulsRuntimeException(e);
        }
    }

    private void clear() {
        cacheManager.clear();
    }

}
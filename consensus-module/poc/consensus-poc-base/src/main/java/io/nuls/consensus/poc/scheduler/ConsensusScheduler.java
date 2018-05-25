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
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.protocol.constant.ProtocolConstant;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ln
 * @date 2018/5/8
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
            ConsensusStatusContext.setConsensusStatus(ConsensusStatus.WAIT_RUNNING);
        } catch (Exception e) {
            Log.warn(e.getMessage());
        }

        threadPool = TaskManager.createScheduledThreadPool(4,
                new NulsThreadFactory(ConsensusConstant.MODULE_ID_CONSENSUS, "consensus-poll-control"));

        BlockProcess blockProcess = new BlockProcess(chainManager, orphanBlockProvider);
        threadPool.scheduleAtFixedRate(new BlockProcessTask(blockProcess), 1000L, 100L, TimeUnit.MILLISECONDS);

        ForkChainProcess forkChainProcess = new ForkChainProcess(chainManager);
        threadPool.scheduleAtFixedRate(new ForkChainProcessTask(forkChainProcess), 1000L, 1000L, TimeUnit.MILLISECONDS);

        ConsensusProcess consensusProcess = new ConsensusProcess(chainManager);
        threadPool.scheduleAtFixedRate(new ConsensusProcessTask(consensusProcess), 1000L, 1000L, TimeUnit.MILLISECONDS);

        orphanBlockProcess = new OrphanBlockProcess(chainManager, orphanBlockProvider);
        orphanBlockProcess.start();

        threadPool.scheduleAtFixedRate(new BlockMonitorProcessTask(new BlockMonitorProcess(chainManager)), 120, 120, TimeUnit.SECONDS);

        threadPool.scheduleAtFixedRate(new RewardStatisticsProcessTask(NulsContext.getServiceBean(RewardStatisticsProcess.class)), ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND, ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND, TimeUnit.SECONDS);
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
            cacheManager.load();
        } catch (Exception e) {
            throw new NulsRuntimeException(e);
        }
    }

    private void clear() {
        cacheManager.clear();
    }
}
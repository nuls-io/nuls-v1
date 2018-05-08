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

package io.nuls.consensus.poc.task;

import io.nuls.consensus.poc.BaseTest;
import io.nuls.consensus.poc.constant.ConsensusStatus;
import io.nuls.consensus.poc.context.ConsensusStatusContext;
import io.nuls.consensus.poc.customer.ConsensusDownloadServiceImpl;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.process.BlockProcess;
import io.nuls.consensus.poc.provider.OrphanBlockProvider;
import io.nuls.kernel.lite.core.SpringLiteContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/8.
 */
public class BlockProcessTaskTest extends BaseTest {

    private BlockProcessTask blockProcessTask;

    @Before
    public void init() {
        ChainManager chainManager = new ChainManager();
        OrphanBlockProvider orphanBlockProvider = new OrphanBlockProvider();

        BlockProcess blockProcess = new BlockProcess(chainManager, orphanBlockProvider);
        blockProcessTask = new BlockProcessTask(blockProcess);
    }

    @Test
    public void testRun() {
        assertNotNull(blockProcessTask);

        ConsensusStatusContext.setConsensusStatus(ConsensusStatus.WAIT_START);

        blockProcessTask.run();

        assert(!ConsensusStatusContext.isRunning());

        ConsensusDownloadServiceImpl downloadService = SpringLiteContext.getBean(ConsensusDownloadServiceImpl.class);
        downloadService.setDownloadSuccess(true);

        blockProcessTask.run();

        assert(ConsensusStatusContext.isRunning());
    }
}
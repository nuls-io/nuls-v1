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

package io.nuls.consensus.poc.task;

import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.process.BlockProcess;
import io.nuls.consensus.poc.provider.BlockQueueProvider;
import io.nuls.consensus.poc.provider.ConsensusSystemProvider;
import io.nuls.consensus.poc.provider.TxQueueProvider;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.poc.service.intf.ConsensusService;

import java.io.IOException;

/**
 * Created by ln on 2018/4/13.
 */
public class BlockProcessTask implements Runnable {

    private BlockProcess blockProcess;
    private BlockQueueProvider blockQueueProvider;
    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);

    public BlockProcessTask(BlockProcess blockProcess, BlockQueueProvider blockQueueProvider) {
        this.blockProcess = blockProcess;
        this.blockQueueProvider = blockQueueProvider;
    }

    @Override
    public void run() {
        //wait consensus ready running
        if(consensusService.getConsensusStatus().ordinal() <= ConsensusStatus.LOADINGCACHE.ordinal()) {
            return;
        }
        BlockContainer blockContainer = null;
        while((blockContainer = blockQueueProvider.get()) != null) {
            try {
                blockProcess.process(blockContainer);
            } catch (IOException e) {
                Log.error("add block fail , error : " + e.getMessage(), e);
            }
        }
    }
}

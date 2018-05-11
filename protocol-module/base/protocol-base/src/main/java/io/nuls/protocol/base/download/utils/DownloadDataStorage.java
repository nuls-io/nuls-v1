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
 *
 */

package io.nuls.protocol.base.download.utils;

import io.nuls.consensus.service.ConsensusServiceIntf;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadDataStorage implements Callable<Boolean> {

    private BlockingQueue<Block> blockQueue;
    private String queueName;
    private boolean running = true;

    private ConsensusServiceIntf consensusService = NulsContext.getServiceBean(ConsensusServiceIntf.class);

    public DownloadDataStorage(BlockingQueue<Block> blockQueue) {
        this.blockQueue = blockQueue;
        this.queueName = queueName;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            Block block;
            while ((block = blockQueue.take()) != null) {
                if (block.getHeader() == null) {
                    break;
                }
                consensusService.addBlock(block);
            }
            return true;
        } catch (InterruptedException e) {
            Log.error(e);
            return false;
        }
    }

}

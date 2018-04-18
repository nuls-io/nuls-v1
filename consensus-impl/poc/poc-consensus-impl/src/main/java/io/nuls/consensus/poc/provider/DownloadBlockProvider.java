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

package io.nuls.consensus.poc.provider;

import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.core.utils.queue.service.impl.QueueService;

/**
 * Created by ln on 2018/4/13.
 */
public class DownloadBlockProvider {

    private final static String QUEUE_NAME = "download-isolated-block-queue";

    private QueueService<BlockContainer> blockQueue;

    public DownloadBlockProvider() {
        blockQueue = new QueueService<>();
        blockQueue.createQueue(QUEUE_NAME, 2000l, false);
    }

    public boolean put(BlockContainer blockContainer) {
        if (blockContainer == null || blockContainer.getBlock() == null) {
            return false;
        }
        blockQueue.offer(QUEUE_NAME, blockContainer);
        return true;
    }

    public BlockContainer get() throws InterruptedException {
        return blockQueue.take(QUEUE_NAME);
    }

    public void clear() {
        blockQueue.clear(QUEUE_NAME);
    }

    public void destory() {
        blockQueue.destroyQueue(QUEUE_NAME);
    }
}

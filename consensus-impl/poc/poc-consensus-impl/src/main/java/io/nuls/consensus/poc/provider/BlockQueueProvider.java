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

import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.protocol.locker.Lockers;
import io.nuls.consensus.poc.protocol.model.container.BlockContainer;
import io.nuls.consensus.poc.protocol.service.DownloadService;
import io.nuls.core.utils.queue.service.impl.QueueService;
import io.nuls.protocol.constant.DownloadStatus;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Block;

/**
 * Created by ln on 2018/4/13.
 */
public class BlockQueueProvider implements QueueProvider {

    private final static String QUEUE_NAME_DOWNLOAD = "download-block-queue";
    private final static String QUEUE_NAME_RECEIVE = "receive-block-queue";

    private QueueService<BlockContainer> blockQueue;

    private DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);

    private boolean downloadBlockQueueHasDestory;

    public BlockQueueProvider() {
        blockQueue = new QueueService<>();
        blockQueue.createQueue(QUEUE_NAME_RECEIVE, 2000l, false);
        createDownloadQueue();
    }

    @Override
    public boolean put(Object data, boolean receive) {
        Lockers.OUTER_LOCK.lock();
        try {
            if (data == null || !(data instanceof Block || data instanceof BlockContainer)) {
                return false;
            }
            BlockContainer blockContainer = null;

            if (data instanceof BlockContainer) {
                blockContainer = (BlockContainer) data;
            }

            if (receive) {
                int status = BlockContainerStatus.RECEIVED;
                if (downloadService.getStatus() != DownloadStatus.SUCCESS) {
                    status = BlockContainerStatus.DOWNLOADING;
                }

                if (blockContainer == null) {
                    blockContainer = new BlockContainer((Block) data, status);
                } else {
                    blockContainer.setStatus(status);
                }

                blockQueue.offer(QUEUE_NAME_RECEIVE, blockContainer);
            } else {
                if (downloadBlockQueueHasDestory) {
                    createDownloadQueue();
                }
                if (blockContainer == null) {
                    blockContainer = new BlockContainer((Block) data, BlockContainerStatus.DOWNLOADING);
                } else {
                    blockContainer.setStatus(BlockContainerStatus.DOWNLOADING);
                }
                blockQueue.offer(QUEUE_NAME_DOWNLOAD, blockContainer);
            }
        } finally {
            Lockers.OUTER_LOCK.unlock();
        }
        return true;
    }

    public BlockContainer get() {

        BlockContainer blockContainer = null;

        Lockers.OUTER_LOCK.lock();
        try {
            //check can destory the download queue
            if (!downloadBlockQueueHasDestory) {
                blockContainer = blockQueue.poll(QUEUE_NAME_DOWNLOAD);
            }

            boolean hasDownloadSuccess = downloadService.getStatus() == DownloadStatus.SUCCESS;
            if (blockContainer == null && hasDownloadSuccess && !downloadBlockQueueHasDestory) {
                downloadBlockQueueHasDestory = true;
                blockQueue.destroyQueue(QUEUE_NAME_DOWNLOAD);
            } else if (hasDownloadSuccess && blockContainer == null) {
                blockContainer = blockQueue.poll(QUEUE_NAME_RECEIVE);
            }
        } finally {
            Lockers.OUTER_LOCK.unlock();
        }
        return blockContainer;
    }

    public long size() {
        long size = blockQueue.size(QUEUE_NAME_RECEIVE);

        if (!downloadBlockQueueHasDestory) {
            size += blockQueue.size(QUEUE_NAME_DOWNLOAD);
        }

        return size;
    }

    @Override
    public void clear() {
        blockQueue.clear(QUEUE_NAME_DOWNLOAD);
        blockQueue.clear(QUEUE_NAME_RECEIVE);
    }

    public void destory() {
        blockQueue.destroyQueue(QUEUE_NAME_DOWNLOAD);
        blockQueue.destroyQueue(QUEUE_NAME_RECEIVE);
    }

    private void createDownloadQueue() {
        blockQueue.createQueue(QUEUE_NAME_DOWNLOAD, 20000l, false);
        downloadBlockQueueHasDestory = false;
    }
}

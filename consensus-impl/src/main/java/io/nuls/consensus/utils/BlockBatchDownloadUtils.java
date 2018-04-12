/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.utils;

import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.entity.DownloadRound;
import io.nuls.consensus.entity.NodeDownloadingStatus;
import io.nuls.consensus.event.GetBlockRequest;
import io.nuls.consensus.manager.BlockManager;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.service.impl.QueueService;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBroadcaster;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BlockBatchDownloadUtils {

    private static final int DOWNLOAD_NODE_COUNT = 10;
    private static final int DOWNLOAD_BLOCKS_PER_TIME = 100;
    /**
     * todo unit:ms
     */
    private static final long DOWNLOAD_IDLE_TIME_OUT = 1000;

    private static final BlockBatchDownloadUtils INSTANCE = new BlockBatchDownloadUtils();
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private QueueService<String> queueService = new QueueService<>();
    private BlockManager blockManager = BlockManager.getInstance();


    private ReceivedTxCacheManager receivedTxCacheManager = ReceivedTxCacheManager.getInstance();
    private ConfirmingTxCacheManager confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();

    private String queueId = StringUtils.getNewUUID();

    private Map<String, NodeDownloadingStatus> nodeStatusMap = new HashMap<>();

    private Map<Long, Block> blockMap = Collections.synchronizedMap(new HashMap<>());

    private boolean finished = true;
    private List<DownloadRound> roundList = new ArrayList<>();
    private DownloadRound currentRound;
    private List<String> nodeIdList;

    private Lock lock = new ReentrantLock();

    private boolean working = false;

    private BlockBatchDownloadUtils() {
    }

    public static BlockBatchDownloadUtils getInstance() {
        return INSTANCE;
    }

    private void init(List<String> nodeIdList) {
        this.nodeIdList = nodeIdList;
        if (!this.queueService.exist(queueId)) {
            this.queueService.createQueue(queueId, (long) nodeIdList.size(), false);
        }
        for (String nodeId : nodeIdList) {
            this.queueService.offer(queueId, nodeId);
        }
        nodeStatusMap.clear();
        blockMap.clear();
    }

    public void request(List<String> nodeIdList, long startHeight, long endHeight) throws InterruptedException {
        if (nodeIdList == null || nodeIdList.isEmpty()) {
            return;
        }
        lock.lock();
        try {
            if (working) {
                return;
            }
            working = true;
            this.init(nodeIdList);
            request(startHeight, endHeight);
            while (working) {
                verify();
                Thread.sleep(500L);
            }
        } catch (Exception e) {
            Log.error(e.getMessage());
            working = false;
            return;
        } finally {
            lock.unlock();
        }
    }

    private void request(long startHeight, long endHeight) throws InterruptedException {
        finished = false;
        roundList.clear();
        long i = startHeight;
        while (true) {
            long start = i;
            int nodeCount = DOWNLOAD_NODE_COUNT;
            if (this.nodeIdList.size() < DOWNLOAD_NODE_COUNT) {
                nodeCount = nodeIdList.size();
            }
            long end = i + DOWNLOAD_BLOCKS_PER_TIME * nodeCount - 1;
            if (end > endHeight) {
                end = endHeight;
            }
            DownloadRound round = new DownloadRound();
            round.setEnd(end);
            round.setStart(start);
            if (i == startHeight) {
                currentRound = round;
                startDownload();
            } else {
                roundList.add(round);
            }
            i = end + 1;
            if (i >= endHeight) {
                break;
            }
        }

    }

    private void startDownload() {
        try {
            Set<String> nodeSet = new HashSet<>();
            if (this.nodeIdList.size() <= DOWNLOAD_NODE_COUNT) {
                nodeSet.addAll(this.nodeIdList);
            } else {
                Random random = new Random();
                while (true) {
                    if (nodeSet.size() >= DOWNLOAD_NODE_COUNT) {
                        break;
                    }
                    int index = random.nextInt(nodeIdList.size());
                    nodeSet.add(this.nodeIdList.get(index));
                }
            }
            List<String> roundNodeList = new ArrayList<>(nodeSet);
            currentRound.setNodeIdList(roundNodeList);
            long end = 0;
            for (int i = 0; end < currentRound.getEnd(); i++) {
                long start = currentRound.getStart() + i * DOWNLOAD_BLOCKS_PER_TIME;
                end = start + DOWNLOAD_BLOCKS_PER_TIME - 1;
                if (end > currentRound.getEnd()) {
                    end = currentRound.getEnd();
                }
                String nodeId = roundNodeList.get(i);
                this.sendRequest(start, end, nodeId);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void sendRequest(long start, long end, String nodeId) {
        NodeDownloadingStatus status = nodeStatusMap.get(nodeId);
        if (status == null) {
            status = new NodeDownloadingStatus();
        }
        status.setDownloadingSet(start, end);
        status.setNodeId(nodeId);
        nodeStatusMap.put(nodeId, status);
        this.eventBroadcaster.sendToNode(new GetBlockRequest(start, end), nodeId);
        status.setUpdateTime(System.currentTimeMillis());
        if (start != end) {
            Log.info("download block :" + start + "-" + end + ",from : " + nodeId);
        }
    }


    public boolean downloadedBlock(String nodeId, Block block) {
        if(!this.working){
            return false;
        }
        try {
            NodeDownloadingStatus status = nodeStatusMap.get(nodeId);
            if (null == status) {
                return false;
            }
            if (!status.containsHeight(block.getHeader().getHeight())) {
                return false;
            }
            ValidateResult result1 = block.verify();
            if (result1.isFailed() && result1.getErrorCode() != ErrorCode.ORPHAN_TX && result1.getErrorCode() != ErrorCode.ORPHAN_BLOCK) {
                Log.info("recieve a block wrong!:" + nodeId + ",blockHash:" + block.getHeader().getHash());
                this.nodeIdList.remove(nodeId);
                if (nodeIdList.isEmpty()) {
                    working = false;
                }
                return true;
            }
            blockMap.put(block.getHeader().getHeight(), block);
            status.downloaded(block.getHeader().getHeight());
            status.setUpdateTime(TimeService.currentTimeMillis());
            if (status.finished()) {
                this.queueService.offer(queueId, nodeId);
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return true;
    }

    private synchronized void verify() {
        List<NodeDownloadingStatus> values = new ArrayList(nodeStatusMap.values());
        for (NodeDownloadingStatus status : values) {
            if (!status.finished() && status.getUpdateTime() < (TimeService.currentTimeMillis() - DOWNLOAD_IDLE_TIME_OUT)) {
                this.queueService.offer(queueId, status.getNodeId());
            }
        }
        if (blockMap.size() != (currentRound.getEnd() - currentRound.getStart() + 1)) {
            for (long i = currentRound.getStart(); i <= currentRound.getEnd(); i++) {
                if (blockMap.containsKey(i)) {
                    continue;
                }
                try {
                    this.failedExecute(i);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
            return;
        }

        for (long i = currentRound.getStart(); i <= currentRound.getEnd(); i++) {
            Block block = blockMap.get(i);
            if (null == block) {
                //todo
                Log.error("cache block is null");
                break;
            }
            if (block.getHeader().getHeight() <= NulsContext.getInstance().getBestHeight()) {
                continue;
            }
            ValidateResult result1 = block.verify();
            if (result1.isFailed() && result1.getErrorCode() != ErrorCode.ORPHAN_TX && result1.getErrorCode() != ErrorCode.ORPHAN_BLOCK) {
                if (null != result1.getMessage()) {
                    Log.debug(result1.getMessage());
                }
                blockMap.remove(block.getHeader().getHeight());

                try {
                    failedExecute(block.getHeader().getHeight());
                } catch (InterruptedException e) {
                    Log.error(e);
                }
                working = false;
                return;
            }
            blockManager.addBlock(block, false, null);
            receivedTxCacheManager.removeTx(block.getTxHashList());
            confirmingTxCacheManager.putTxList(block.getTxs());
        }
        finished();
    }


    private void failedExecute(long height) throws InterruptedException {
        if (nodeIdList.isEmpty()) {
            return;
        }
        if (blockMap.containsKey(height)) {
            return;
        }
//        if (this.queueService.size(queueId) == 0) {
//            this.queueService.offerList(queueId, nodeIdList);
//        }
        this.sendRequest(height, height, this.queueService.take(queueId));
    }

    private void finished() {
        if (!roundFinished()) {
            return;
        }
        blockMap.clear();
        nodeStatusMap.clear();
        if (!roundList.isEmpty()) {
            currentRound = roundList.get(0);
            roundList.remove(0);
            startDownload();
            return;
        }
        this.finished = true;
        this.queueService.destroyQueue(queueId);
        this.nodeStatusMap.clear();
        working = false;
    }

    private boolean roundFinished() {
        return blockMap.size() == (currentRound.getEnd() - currentRound.getStart() + 1);
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isWorking() {
        return working;
    }
}

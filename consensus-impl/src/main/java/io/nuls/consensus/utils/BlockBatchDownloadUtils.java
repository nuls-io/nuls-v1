/**
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
package io.nuls.consensus.utils;

import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.entity.DownloadRound;
import io.nuls.consensus.entity.NodeDownloadingStatus;
import io.nuls.consensus.event.GetBlockRequest;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.service.impl.QueueService;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.entity.NodePo;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.service.NetworkService;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BlockBatchDownloadUtils {

    private static final int DOWNLOAD_NODE_COUNT = 10;
    private static final int DOWNLOAD_BLOCKS_PER_TIME = 10;
    /**
     * unit:ms
     */
    private static final long DOWNLOAD_IDLE_TIME_OUT = 1000;

    private static final BlockBatchDownloadUtils INSTANCE = new BlockBatchDownloadUtils();
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private QueueService<String> queueService = NulsContext.getServiceBean(QueueService.class);
    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);


    private ReceivedTxCacheManager receivedTxCacheManager = ReceivedTxCacheManager.getInstance();
    private ConfirmingTxCacheManager confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();

    private String queueId = StringUtils.getNewUUID();

    private Map<String, NodeDownloadingStatus> nodeStatusMap = new HashMap<>();

    private Map<Long, Block> blockMap = new HashMap<>();

    private boolean finished = true;
    private List<DownloadRound> roundList = new ArrayList<>();
    private DownloadRound currentRound;
    private List<String> nodeIdList;
    private BlockInfo blocksHash;

    private Lock lock = new ReentrantLock();

    private boolean working = false;

    private BlockBatchDownloadUtils() {
    }

    public static BlockBatchDownloadUtils getInstance() {
        return INSTANCE;
    }

    private void init(List<String> nodeIdList) {
        this.nodeIdList = nodeIdList;
        this.queueService.createQueue(queueId, (long) nodeIdList.size(), false);
        for (String nodeId : nodeIdList) {
            this.queueService.offer(queueId, nodeId);
        }
        nodeStatusMap.clear();
        blockMap.clear();
    }

    public void request(List<String> nodeIdList, long startHeight, long endHeight) throws InterruptedException {
        lock.lock();
        if(working){
            return;
        }
        working = true;
        this.init(nodeIdList);
        blocksHash = DistributedBlockInfoRequestUtils.getInstance().request(startHeight, endHeight, DOWNLOAD_BLOCKS_PER_TIME);
        request(startHeight, endHeight);
    }

    private void request(long startHeight, long endHeight) throws InterruptedException {
        finished = false;
        roundList.clear();
        for (long i = startHeight; i <= endHeight; ) {
            long start = i;
            long end = i + DOWNLOAD_BLOCKS_PER_TIME * DOWNLOAD_NODE_COUNT;
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
            i = end;
        }

    }

    private void startDownload() {
        while (true) {
            try {
                Set<String> nodeSet = new HashSet<>();
                Random random = new Random();
                while (true) {
                    if (nodeSet.size() >= DOWNLOAD_NODE_COUNT) {
                        break;
                    }
                    int index = random.nextInt(nodeIdList.size() - 1) % (nodeIdList.size());
                    nodeSet.add(this.nodeIdList.get(index));
                }
                List<String> roundNodeList = new ArrayList<>(nodeSet);
                long end = 0;
                for (int i = 0; end < currentRound.getEnd(); i++) {
                    long start = currentRound.getStart() + i * DOWNLOAD_BLOCKS_PER_TIME;
                    end = start + DOWNLOAD_BLOCKS_PER_TIME;
                    if (end > currentRound.getEnd()) {
                        end = currentRound.getEnd();
                    }
                    this.sendRequest(start, end, roundNodeList.get(i));
                }
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }


    private void sendRequest(long start, long end, String nodeId) {
        NodeDownloadingStatus status = new NodeDownloadingStatus();
        status.setStart(start);
        status.setEnd(end);
        status.setNodeId(nodeId);
        this.eventBroadcaster.sendToNode(new GetBlockRequest(start, end), nodeId);
        status.setUpdateTime(System.currentTimeMillis());
        nodeStatusMap.put(nodeId, status);
    }


    public boolean downloadedBlock(String nodeId, Block block) {
        NodeDownloadingStatus status = nodeStatusMap.get(nodeId);
        if (null == status) {
            return false;
        }
        if (status.containsHeight(block.getHeader().getHeight())) {
            return false;
        }
        blockMap.put(block.getHeader().getHeight(), block);
        status.downloaded(block.getHeader().getHeight());
        status.setUpdateTime(System.currentTimeMillis());
        if (status.finished()) {
            this.queueService.offer(queueId, nodeId);
        }
        verify();
        return true;
    }

    private void verify() {
        boolean done = true;
        for (NodeDownloadingStatus status : nodeStatusMap.values()) {
            if (done && status.finished()) {
                continue;
            }
            done = false;
        }
        if(!done){
            return;
        }
        Result result;
        try {
            result = checkHash();
        } catch (InterruptedException e) {
            Log.error(e);
            return;
        }
        if (null == result || result.isFailed()) {
            return;
        }
        for (long i = currentRound.getStart(); i <= currentRound.getEnd(); i++) {
            Block block = blockMap.get(i);
            ValidateResult result1 = block.verify();
            if (result1.isFailed()) {
                try {
                    failedExecute(block.getHeader().getHeight());
                } catch (InterruptedException e) {
                    Log.error(e);
                }
                return;
            }
            blockCacheManager.cacheBlock(block);
            receivedTxCacheManager.removeTx(block.getTxHashList());
            confirmingTxCacheManager.putTxList(block.getTxs());
        }
        finished();
    }

    private void failedExecute(long height) throws InterruptedException {
        NodeDownloadingStatus nodeStatus = null;
        for (NodeDownloadingStatus status : nodeStatusMap.values()) {
            if (status.containsHeight(height)) {
                nodeStatus = status;
            }
        }
        if (null == nodeStatus) {
            return;
        }
        networkService.blackNode(nodeStatus.getNodeId(), NodePo.YELLOW);
        this.nodeIdList.remove(nodeIdList);
        this.queueService.remove(queueId, nodeStatus.getNodeId());
        this.sendRequest(nodeStatus.getStart(), nodeStatus.getEnd(), this.queueService.take(queueId));
    }

    private Result checkHash() throws InterruptedException {
        for (long i = currentRound.getStart(); i >= currentRound.getEnd(); i++) {
            Block block = blockMap.get(i);
            if ((i - currentRound.getStart()) % DOWNLOAD_BLOCKS_PER_TIME == 0) {
                NulsDigestData mustHash = blocksHash.getHash(block.getHeader().getHeight());
                NulsDigestData hash = block.getHeader().getHash();
                if (null == hash || null == mustHash || !mustHash.getDigestHex().equals(hash.getDigestHex())) {
                    failedExecute(block.getHeader().getHeight());
                    return Result.getFailed("hash wrong!");
                }
            }
            String preHash = block.getHeader().getPreHash().getDigestHex();
            Block preBlock = blockMap.get(preHash);
            if (preBlock == null) {
                preBlock = blockService.getBlock(preHash);
            }
            if (null == preBlock || preBlock.getHeader().getHeight() != (block.getHeader().getHeight() - 1)) {
                failedExecute(block.getHeader().getHeight());
                return Result.getFailed("prehash wrong!");
            }
        }
        return Result.getSuccess();
    }

    private void finished() {
        this.finished = true;
        this.queueService.destroyQueue(queueId);
        this.nodeStatusMap.clear();
        if (!roundList.isEmpty()) {
            currentRound = roundList.get(0);
            roundList.remove(0);
            startDownload();
        }
        working = false;
        lock.unlock();
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isWorking() {
        return working;
    }
}

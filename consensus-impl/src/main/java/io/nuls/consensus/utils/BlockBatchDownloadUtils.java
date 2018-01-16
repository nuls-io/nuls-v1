package io.nuls.consensus.utils;

import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.entity.DownloadRound;
import io.nuls.consensus.event.GetBlockRequest;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.service.impl.QueueService;
import io.nuls.core.utils.str.StringUtils;
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
    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);
    private QueueService<String> queueService = NulsContext.getInstance().getService(QueueService.class);
    private BlockCacheManager blockCacheManager = NulsContext.getInstance().getService(BlockCacheManager.class);
    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);

    private ReceivedTxCacheManager receivedTxCacheManager = ReceivedTxCacheManager.getInstance();
    private ConfirmingTxCacheManager confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();

    private String queueId = StringUtils.getNewUUID();
    private String roundQueueId = StringUtils.getNewUUID();

    private Map<String, Long> nodeEndHeightMap = new HashMap<>();
    private Map<String, Long> nodeStartHeightMap = new HashMap<>();

    private Map<String, Long> nodeTimeMap = new HashMap<>();

    private Map<Long, Block> blockMap = new HashMap<>();

    private boolean finished = true;
    private List<DownloadRound> roundList = new ArrayList<>();
    private DownloadRound currentRound;
    private List<String> nodeIdList;
    private String highestHash;

    private Lock lock = new ReentrantLock();

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
        nodeEndHeightMap.clear();
        nodeStartHeightMap.clear();
        nodeTimeMap.clear();
        blockMap.clear();
    }

    public void request(List<String> nodeIdList, long startHeight, long endHeight, String highestHash) throws InterruptedException {
        lock.lock();
        this.init(nodeIdList);
        this.highestHash = highestHash;
        request(startHeight, endHeight);
    }

    private void request(long startHeight, long endHeight) throws InterruptedException {
        finished = false;
        this.queueService.createQueue(roundQueueId, (endHeight - startHeight) / (DOWNLOAD_BLOCKS_PER_TIME * DOWNLOAD_NODE_COUNT) + 1, false);
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
            }
            roundList.add(round);
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
        nodeTimeMap.put(nodeId, System.currentTimeMillis());
        GetBlockRequest event = new GetBlockRequest(start, end);
        this.eventBroadcaster.sendToNode(event, nodeId);
        //todo 缓存
    }


    public boolean downloadedBlock(String nodeId, Block block) {
        Long start = nodeStartHeightMap.get(nodeId);
        Long end = nodeEndHeightMap.get(nodeId);
        if (null == start || null == end) {
            return false;
        }
        if (!this.nodeIdList.contains(nodeId) || start > block.getHeader().getHeight() || end < block.getHeader().getHeight()) {
            return false;
        }
        blockMap.put(block.getHeader().getHeight(), block);
        //todo 判断是否完成、标记时间
        if (block.getHeader().getHeight() == end) {
            this.queueService.offer(queueId, nodeId);
        }

        verify();
        return true;
    }

    private void verify() {
        if (blockMap.size() != (endHeight - startHeight + 1)) {
            return;
        }
        Result result = checkHash();
        if (null == result || result.isFailed()) {
            return;
        }
        for (long i = 0; i <= (endHeight - startHeight); i++) {
            Block block = blockMap.get(startHeight + i);
            block.verify();
            blockCacheManager.cacheBlock(block);
            receivedTxCacheManager.removeTx(block.getTxHashList());
            confirmingTxCacheManager.putTxList(block.getTxs());
        }
        finished();
    }

    private Result checkHash() {
        String hash = highestHash;
        for (long i = endHeight; i >= startHeight; i--) {
            Block block = blockMap.get(i);
            if (block.getHeader().getHash().getDigestHex().equals(hash)) {
                hash = block.getHeader().getPreHash().getDigestHex();
                continue;
            }
            String nodeId = heightNodeMap.get(i);
            networkService.removeNode(nodeId);
            this.queueService.remove(queueId, nodeId);
            if (this.queueService.size(queueId) == 0) {
                throw new NulsRuntimeException(ErrorCode.NET_MESSAGE_ERROR, "download block faild!");
            }
            try {
                this.sendRequest(i, this.queueService.take(queueId));
            } catch (InterruptedException e) {
                Log.error(e);
            }
            return Result.getFailed("");
        }
        return Result.getSuccess();
    }

    private void finished() {
        this.finished = true;
        this.queueService.destroyQueue(queueId);
        this.queueService.destroyQueue(roundQueueId);
        lock.unlock();
    }

    public boolean isFinished() {
        return finished;
    }
}

package io.nuls.consensus.utils;

import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.core.chain.entity.BasicTypeData;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class DistributedBlockDownloadUtils {
    private static final DistributedBlockDownloadUtils INSTANCE = new DistributedBlockDownloadUtils();
    private String queueId = StringUtils.getNewUUID();
    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);
    private QueueService<String> queueService = NulsContext.getInstance().getService(QueueService.class);
    private BlockCacheManager blockCacheManager = NulsContext.getInstance().getService(BlockCacheManager.class);
    private Map<Long, String> heightNodeMap = new HashMap<>();
    private Map<Long, Block> blockMap = new HashMap<>();
    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);
//    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
private ReceivedTxCacheManager receivedTxCacheManager = ReceivedTxCacheManager.getInstance();
    private ConfirmingTxCacheManager confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();
    private boolean finished = true;
    private List<String> nodeIdList;
    private long startHeight;
    private long endHeight;
    private String highestHash;

    private Lock lock = new ReentrantLock();

    private DistributedBlockDownloadUtils() {
    }

    public static DistributedBlockDownloadUtils getInstance() {
        return INSTANCE;
    }

    private void init(List<String> nodeIdList) {
        this.nodeIdList = nodeIdList;
        this.queueService.createQueue(queueId, (long) nodeIdList.size(), false);
        for (String nodeId : nodeIdList) {
            this.queueService.offer(queueId, nodeId);
        }
        heightNodeMap.clear();
        blockMap.clear();
    }

    public void request(List<String> nodeIdList, long startHeight, long endHeight, String highestHash) throws InterruptedException {
        lock.lock();
        this.init(nodeIdList);
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.highestHash = highestHash;
        finished = false;
        for (long i = 0; i <= (endHeight - startHeight); i++) {
            sendRequest(i + startHeight, this.queueService.take(queueId));
        }
    }

    private void sendRequest(long height, String nodeId) {
        heightNodeMap.put(height, nodeId);
        GetBlockEvent event = new GetBlockEvent();
        event.setEventBody(new BasicTypeData<>(height));
        this.eventBroadcaster.sendToNode(event, nodeId);
    }


    public boolean recieveBlock(String nodeId, Block block) {
        if (!this.nodeIdList.contains(nodeId) || !nodeId.equals(heightNodeMap.get(block.getHeader().getHeight()))) {
            return false;
        }
        blockMap.put(block.getHeader().getHeight(), block);
        this.queueService.offer(queueId, nodeId);
        verify();
        return true;
    }

    private void verify() {
        if (blockMap.size() != (endHeight - startHeight + 1)) {
            return;
        }
        Result result = checkHash();
        if (null==result||result.isFailed()) {
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
        lock.unlock();
    }

    public boolean isFinished() {
        return finished;
    }
}

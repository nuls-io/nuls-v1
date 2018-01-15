package io.nuls.consensus.utils;

import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.event.GetBlockRequest;
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
public class BlockBatchDownloadUtil {

    private static final int DOWNLOAD_NODE_COUNT = 10;
    private static final int DOWNLOAD_BLOCKS_PER_TIME = 10;
    /**
     * unit:ms
     */
    private static final long DOWNLOAD_IDLE_TIME_OUT = 1000;

    private static final BlockBatchDownloadUtil INSTANCE = new BlockBatchDownloadUtil();

    private String queueId = StringUtils.getNewUUID();
    private List<String> nodeIdList;
    private long startHeight;
    private long endHeight;
    private String highestHash;

    private Lock lock = new ReentrantLock();

    private BlockBatchDownloadUtil() {
    }

    public static BlockBatchDownloadUtil getInstance() {
        return INSTANCE;
    }

    private void init(List<String> nodeIdList) {
        this.nodeIdList = nodeIdList;


    }

    public void request(List<String> nodeIdList, long startHeight, long endHeight, String highestHash) throws InterruptedException {
        lock.lock();
        this.init(nodeIdList);
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.highestHash = highestHash;
//        查询分段hash

        //request(startHeight, endHeight);
    }
}

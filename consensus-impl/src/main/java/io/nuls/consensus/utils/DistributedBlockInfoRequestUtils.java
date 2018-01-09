package io.nuls.consensus.utils;

import io.nuls.consensus.event.GetBlockHeaderEvent;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.EventBroadcaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class DistributedBlockInfoRequestUtils {
    private static final DistributedBlockInfoRequestUtils INSTANCE = new DistributedBlockInfoRequestUtils();
    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);
    private List<String> nodeIdList;
    private Map<String, BlockHeader> headerMap = new HashMap<>();
    /**
     * list order by answered time
     */
    private Map<String, List<String>> calcMap = new HashMap<>();
    private BlockInfo bestBlockInfo;
    private long askHeight;
    private Lock lock = new ReentrantLock();
    private boolean requesting;
    private long startTime;

    private DistributedBlockInfoRequestUtils() {
    }

    public static DistributedBlockInfoRequestUtils getInstance() {
        return INSTANCE;
    }

    /**
     * default:0,get best height;
     *
     * @param height
     */
    public BlockInfo request(long height) {
        lock.lock();
        this.startTime = TimeService.currentTimeMillis();
        requesting = true;
        headerMap.clear();
        calcMap.clear();
        askHeight = height;
        GetBlockHeaderEvent getBlockHeaderEvent;
        if (0 > height) {
            getBlockHeaderEvent = new GetBlockHeaderEvent();
        } else {
            getBlockHeaderEvent = new GetBlockHeaderEvent(height);
        }
        nodeIdList = this.eventBroadcaster.broadcastAndCache(getBlockHeaderEvent,false);
        if (nodeIdList.isEmpty()) {
            Log.error("get best height from net faild!");
            lock.unlock();
            throw new NulsRuntimeException(ErrorCode.NET_MESSAGE_ERROR, "broadcast faild!");
        }
        return this.getBlockInfo();
    }


    public boolean addBlockHeader(String nodeId, BlockHeader header) {
        if (!headerMap.containsKey(nodeId)) {
            return false;
        }
        if (!requesting) {
            return false;
        }
        headerMap.put(nodeId, header);
        String key = header.getHeight()+""+header.getHash();
        List<String> nodes = calcMap.get(key);
        if (null == nodes) {
            nodes = new ArrayList<>();
        }
        if (!nodes.contains(nodeId)) {
            nodes.add(nodeId);
        }
        calcMap.put(key, nodes);
        calc();
        return true;
    }

    private void calc() {
        if (null == nodeIdList || nodeIdList.isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "success list of nodes is empty!");
        }

        int size = nodeIdList.size();
        int halfSize = (size + 1) / 2;
        if (headerMap.size() < halfSize) {
            return;
        }
        BlockInfo result = null;
        for (String key : calcMap.keySet()) {
            List<String> nodes = calcMap.get(key);
            if (nodes.size() > halfSize) {
                result = new BlockInfo();
                BlockHeader header = headerMap.get(result.getNodeIdList().get(0));
                result.setHash(header.getHash());
                result.setHeight(header.getHeight());
                result.setNodeIdList(nodes);
                result.setFinished(true);
                break;
            }
        }
        if (null != result) {
            bestBlockInfo = result;
        } else if (size == calcMap.size()) {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            this.request(askHeight);
        }

    }

    private BlockInfo getBlockInfo() {
        while (true) {
            if (null != bestBlockInfo && bestBlockInfo.isFinished()) {
                break;
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            if((TimeService.currentTimeMillis()-startTime)>10000L){
                lock.unlock();
              throw new NulsRuntimeException(ErrorCode.TIME_OUT);
            }
        }
        BlockInfo info = bestBlockInfo;
        bestBlockInfo = null;
        requesting = false;
        lock.unlock();
        return info;
    }
}

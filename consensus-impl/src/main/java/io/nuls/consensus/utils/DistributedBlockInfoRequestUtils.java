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

import io.nuls.consensus.entity.BlockHashResponse;
import io.nuls.consensus.event.GetBlocksHashRequest;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
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
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private List<String> nodeIdList;
    private Map<String, BlockHashResponse> hashesMap = new HashMap<>();
    /**
     * list order by answered time
     */
    private Map<String, List<String>> calcMap = new HashMap<>();
    private BlockInfo bestBlockInfo;
    private long start, end, split;
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
        return this.request(height, height, 1);
    }

    public BlockInfo request(long start, long end, long split) {
        lock.lock();
        this.startTime = TimeService.currentTimeMillis();
        requesting = true;
        hashesMap.clear();
        calcMap.clear();
        this.start = start;
        this.end = end;
        this.split = split;
        GetBlocksHashRequest event = new GetBlocksHashRequest(start, end, split);
        nodeIdList = this.eventBroadcaster.broadcastAndCache(event, false);
        if (nodeIdList.isEmpty()) {
            Log.error("get best height from net faild!");
            lock.unlock();
            return null;
        }

        return this.getBlockInfo();
    }


    public boolean addBlockHashResponse(String nodeId, BlockHashResponse response) {
        if (!this.nodeIdList.contains(nodeId)) {
            return false;
        }
        if (!requesting) {
            return false;
        }
        if (hashesMap.get(nodeId) == null) {
            hashesMap.put(nodeId, response);
        } else {
            BlockHashResponse instance = hashesMap.get(nodeId);
            instance.merge(response);
            hashesMap.put(nodeId, instance);
        }
        if (response.getHeightList().get(response.getHeightList().size() - 1) < end) {
            return true;
        }
        String key = response.getHash().getDigestHex();
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
        if (hashesMap.size() <= halfSize) {
            return;
        }
        BlockInfo result = null;
        for (String key : calcMap.keySet()) {
            List<String> nodes = calcMap.get(key);
            if (nodes.size() > halfSize) {
                result = new BlockInfo();
                BlockHashResponse response = hashesMap.get(result.getNodeIdList().get(0));
                Long bestHeight = 0L;
                NulsDigestData bestHash = null;
                for (int i = 0; i < response.getHeightList().size(); i++) {
                    Long height = response.getHeightList().get(i);
                    NulsDigestData hash = response.getHashList().get(i);
                    if (height > bestHeight) {
                        bestHash = hash;
                        bestHeight = height;
                    }
                    result.putHash(height, hash);
                }
                result.setBestHash(bestHash);
                result.setBestHeight(bestHeight);
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
            this.request(start, end, split);
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
            if ((TimeService.currentTimeMillis() - startTime) > 10000L) {
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

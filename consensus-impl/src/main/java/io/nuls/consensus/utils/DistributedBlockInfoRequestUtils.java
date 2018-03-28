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

import io.nuls.consensus.entity.BlockHashResponse;
import io.nuls.consensus.event.GetBlocksHashRequest;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class DistributedBlockInfoRequestUtils {
    private static final DistributedBlockInfoRequestUtils INSTANCE = new DistributedBlockInfoRequestUtils();
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private List<String> nodeIdList;
    private Map<String, BlockHashResponse> hashesMap = new ConcurrentHashMap<>();
    /**
     * list order by answered time
     */
    private Map<String, List<String>> calcMap = new ConcurrentHashMap<>();
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
     * @param sendList
     */
    public BlockInfo request(long height, List<String> sendList) {
        return this.request(height, height, 1, sendList);
    }

    public BlockInfo request(long start, long end, long split, List<String> sendList) {
        lock.lock();
        try {
            requesting = true;
            hashesMap.clear();
            calcMap.clear();
            this.start = start;
            this.end = end;
            this.split = split;
            GetBlocksHashRequest event = new GetBlocksHashRequest(start, end, split);
            this.startTime = TimeService.currentTimeMillis();
            if (null == sendList) {
                this.nodeIdList = new ArrayList<>();
                List<Node> nodes = networkService.getAvailableNodes();
                for (Node node : nodes) {
                    this.nodeIdList.add(node.getId());
                }
            } else {
                this.nodeIdList = sendList;
            }
            if (nodeIdList.isEmpty()) {
                return null;
            }
            for (String nodeId : nodeIdList) {
                boolean result = this.eventBroadcaster.sendToNode(event, nodeId);
                if (!result) {
                    this.nodeIdList.remove(nodeId);
                }
            }
            BlockInfo bi = this.getBlockInfo();
            if (start == end && start <= 0 && bi.getNodeIdList().size() >= (nodeIdList.size() / 2)) {
                if (bi == null) {
                    NulsContext.getInstance().setNetBestBlockHeight(null);
                }
                NulsContext.getInstance().setNetBestBlockHeight(bi.getBestHeight());
            }
            return bi;
        } catch (Exception e) {
            throw e;
        } finally {
            lock.unlock();
        }
    }


    public boolean addBlockHashResponse(String nodeId, BlockHashResponse response) {
        if (this.nodeIdList == null || !this.nodeIdList.contains(nodeId)) {
            return false;
        }
        if (!requesting) {
            return false;
        }
        if (response.getBestHeight() == 0 && NulsContext.getInstance().getBestHeight() > 0) {
            hashesMap.remove(nodeId);
            nodeIdList.remove(nodeId);
            return false;
        }
        if (hashesMap.get(nodeId) == null) {
            hashesMap.put(nodeId, response);
        } else {
            BlockHashResponse instance = hashesMap.get(nodeId);
            instance.merge(response);
            hashesMap.put(nodeId, instance);
        }
//        if (response.getHeightList().get(response.getHeightList().size() - 1) < end) {
//            return true;
//        }
        String key = response.getBestHash().getDigestHex();
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
        //
        if (hashesMap.size() < halfSize) {
            return;
        }
        BlockInfo result = null;
        for (String key : calcMap.keySet()) {
            List<String> nodes = calcMap.get(key);
            if (nodes == null) {
                continue;
            }
            //todo =
            if (nodes.size() >= halfSize) {
                result = new BlockInfo();
                BlockHashResponse response = hashesMap.get(nodes.get(0));
                if (response == null || response.getHeightList() == null) {
                    //todo check it
                    continue;
                }
                Long bestHeight = 0L;
                NulsDigestData bestHash = null;
                for (int i = 0; i < response.getHeightList().size(); i++) {
                    Long height = response.getHeightList().get(i);
                    NulsDigestData hash = response.getHashList().get(i);
                    if (height >= bestHeight) {
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
        }
//        else if (size == calcMap.size()) {
//            try {
//                Thread.sleep(2000L);
//            } catch (InterruptedException e) {
//                Log.error(e);
//            }
//            try {
//                this.request(start, end, split);
//            } catch (Exception e) {
//                Log.error(e.getMessage());
//            }
//        }

    }

    private BlockInfo getBlockInfo() {
        while (true) {
            if (null != bestBlockInfo && bestBlockInfo.isFinished()) {
                break;
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            long timeout = 10000L;

            if ((TimeService.currentTimeMillis() - startTime) > (timeout - 1000L) && hashesMap.size() >= ((nodeIdList.size() + 1) / 2) && end <= 0) {
                long localHeight = NulsContext.getInstance().getBestBlock().getHeader().getHeight();
                long minHeight = Long.MAX_VALUE;
                NulsDigestData minHash = null;
                List<String> nodeIds = new ArrayList<>();
                try {
                    for (String nodeId : hashesMap.keySet()) {
                        BlockHashResponse response = hashesMap.get(nodeId);
                        long height = response.getHeightList().get(0);
                        NulsDigestData hash = response.getHashList().get(0);
                        if (height >= localHeight) {
                            if (height <= minHeight) {
                                minHeight = height;
                                minHash = hash;
                            }
                            nodeIds.add(nodeId);
                        }
                    }
                } catch (Exception e) {
                    break;
                }
                BlockInfo result = new BlockInfo();
                result.putHash(minHeight, minHash);
                result.setBestHash(minHash);
                result.setBestHeight(minHeight);
                result.setNodeIdList(nodeIds);
                result.setFinished(true);
                if (result.getBestHeight() < Long.MAX_VALUE) {
                    bestBlockInfo = result;
                } else {
                    throw new NulsRuntimeException(ErrorCode.TIME_OUT);
                }
            }else if ((TimeService.currentTimeMillis() - startTime) > timeout && !(hashesMap.size() >= ((nodeIdList.size() + 1) / 2))) {
                throw new NulsRuntimeException(ErrorCode.TIME_OUT);
            }
        }
        BlockInfo info = bestBlockInfo;
        bestBlockInfo = null;
        requesting = false;
        return info;
    }
}
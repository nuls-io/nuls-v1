/**
 * MIT License
 **
 * Copyright (c) 2017-2018 nuls.io
 **
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 **
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 **
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
import io.nuls.core.chain.entity.Block;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private CopyOnWriteArrayList<String> nodeIdList;
    private Map<String, BlockHashResponse> hashesMap = new ConcurrentHashMap<>();
    /**
     * list order by answered time
     */
    private Map<String, List<String>> calcMap = new ConcurrentHashMap<>();
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
     */
    public BlockInfo request(long height) {
        lock.lock();
        try {
            requesting = true;
            hashesMap.clear();
            calcMap.clear();
            this.askHeight = height;
            GetBlocksHashRequest event = new GetBlocksHashRequest(askHeight, askHeight);
            this.startTime = TimeService.currentTimeMillis();
            this.nodeIdList = new CopyOnWriteArrayList<>();
            Collection<Node> nodes = networkService.getAvailableNodes();
            for (Node node : nodes) {
                this.nodeIdList.add(node.getId());
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
            if (bi == null) {
                NulsContext.getInstance().setNetBestBlockHeight(null);
            }
            NulsContext.getInstance().setNetBestBlockHeight(bi.getBestHeight());
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
        if (this.askHeight>0&&this.askHeight != response.getBestHeight()) {
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
            BlockInfo blockInfo = new BlockInfo();
            blockInfo.setBestHeight(0);
            blockInfo.setBestHash(NulsContext.getInstance().getGenesisBlock().getHeader().getHash());
            blockInfo.putHash(0, blockInfo.getBestHash());
            blockInfo.setNodeIdList(this.nodeIdList);
            blockInfo.setFinished(true);
            this.bestBlockInfo = blockInfo;
            return;
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

            if ((TimeService.currentTimeMillis() - startTime) > timeout && hashesMap.size() >= (1 + nodeIdList.size() / 2)) {
                int maxSize = 0;
                List<String> nodeIds = null;
                try {
                    for (String key : calcMap.keySet()) {
                        List<String> ids = calcMap.get(key);
                        if (ids.size() > maxSize) {
                            maxSize = ids.size();
                            nodeIds = ids;
                        } else if (ids.size() == maxSize) {
                            BlockHashResponse response_a = hashesMap.get(nodeIds.get(0));
                            long height_a = response_a.getBestHeight();
                            BlockHashResponse response_b = hashesMap.get(ids.get(0));
                            long height_b = response_b.getBestHeight();
                            if (height_b > height_a) {
                                nodeIds = ids;
                            }
                        }
                    }
                } catch (Exception e) {
                    break;
                }
                if (null == nodeIds || nodeIds.isEmpty()) {
                    continue;
                }
                BlockHashResponse response = hashesMap.get(nodeIds.get(0));
                BlockInfo result = new BlockInfo();
                result.putHash(response.getBestHeight(), response.getBestHash());
                result.setBestHash(response.getBestHash());
                result.setBestHeight(response.getBestHeight());
                result.setNodeIdList(nodeIds);
                result.setFinished(true);
                bestBlockInfo = result;
            } else if ((TimeService.currentTimeMillis() - startTime) > timeout) {
                throw new NulsRuntimeException(ErrorCode.TIME_OUT);
            } else {
                calc();
            }
        }
        BlockInfo info = bestBlockInfo;
        bestBlockInfo = null;
        requesting = false;
        return info;
    }
}
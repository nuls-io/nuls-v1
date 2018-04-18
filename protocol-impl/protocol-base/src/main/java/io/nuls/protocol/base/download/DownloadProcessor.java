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

package io.nuls.protocol.base.download;

import io.nuls.consensus.poc.protocol.constant.PocConsensusConstant;
import io.nuls.consensus.poc.protocol.service.BlockService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.NulsThreadFactory;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.service.impl.QueueService;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.constant.DownloadStatus;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Block;

import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadProcessor extends Thread {

    private static DownloadProcessor INSTANCE = new DownloadProcessor();

    private DownloadStatus downloadStatus = DownloadStatus.WAIT;

    private ScheduledThreadPoolExecutor threadPool;

    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    private DownloadProcessor() {
    }

    public static DownloadProcessor getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {

        boolean isContinue = checkNetworkAndStatus();

        if(!isContinue) {
            return;
        }

        //检查网络不再有变化，一般触发同步的节点数量有限，为确保数据的准确性和安全性，尽量等待连上更多的节点之后开始同步
        //There is no change in the inspection network. Usually, the number of nodes that trigger synchronization is limited.
        // To ensure the accuracy and security of data, try to wait for more nodes to start synchronization.
//        waitNetworkNotChange();
        downloadStatus = DownloadStatus.READY;

        doSynchronize();
    }

    /**
     * 区块同步流程
     * block synchronization process
     */
    private void doSynchronize() {

        if(downloadStatus != DownloadStatus.READY) {
            return;
        }

        downloadStatus = DownloadStatus.DOWNLOADING;

        //查找网络大多数节点一致的最高区块hash
        //Finding the highest block hash consistent with most nodes in the network
        NetworkNewestBlockInfos newestInfos = getNetworkNewestBlockInfos();

        if(newestInfos.getNodes().size() < 1) {
            //TODO
            downloadStatus = DownloadStatus.WAIT;
            return;
        }

        QueueService<Block> blockQueue = new QueueService<Block>();

        String queueName = "synchronize-block-queue";

        blockQueue.createQueue(queueName, (long) Integer.MAX_VALUE, false);

        DownloadThreadManager downloadThreadManager = new DownloadThreadManager(newestInfos, blockQueue, queueName);

        FutureTask<Boolean> threadManagerFuture = new FutureTask<>(downloadThreadManager);

        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_CONSENSUS, "download-thread-manager",
                new Thread(threadManagerFuture));

        DownloadDataStorage downloadDataStorage = new DownloadDataStorage(blockQueue, queueName);

        FutureTask<Boolean> dataStorageFuture = new FutureTask<>(downloadDataStorage);

        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_CONSENSUS, "download-data-storeage",
                new Thread(dataStorageFuture));

        try {
            Boolean downResult = threadManagerFuture.get();

            blockQueue.offer(queueName, new Block());

            Boolean storageResult = dataStorageFuture.get();

            boolean success = downResult != null && downResult.booleanValue() && storageResult != null && storageResult.booleanValue();

            if(success && checkIsNewest()) {
                downloadStatus = DownloadStatus.SUCCESS;
            } else if(downloadStatus != DownloadStatus.WAIT){
                downloadStatus = DownloadStatus.FAILED;
            }
        } catch (Exception e) {
            Log.error(e);
            downloadStatus = DownloadStatus.FAILED;
        } finally {
            blockQueue.destroyQueue(queueName);
        }
    }

    private boolean checkIsNewest() {
        NetworkNewestBlockInfos newestInfos = getNetworkNewestBlockInfos();
//        if(!blockService.getBestBlock().getHeader().getHash().getDigestHex().equals(newestInfos.getNetBestHash())) {
//            downloadStatus = DownloadStatus.WAIT;
//            return false;
//        }
        if(newestInfos.getNetBestHeight() > blockService.getLocalBestBlock().getHeader().getHeight()) {
            downloadStatus = DownloadStatus.WAIT;
            return false;
        }
        return true;
    }

    /**
     * 获取网络中对等节点的最新区块信息
     * Get latest block information of peer nodes in the network
     * @return NetworkNewestBlockInfos
     */
    private NetworkNewestBlockInfos getNetworkNewestBlockInfos() {

        NetworkNewestBlockInfos infos = getNetworkNewestBlock();

        return infos;
    }

    public NetworkNewestBlockInfos getNetworkNewestBlock() {

        Collection<Node> nodeList = networkService.getAvailableNodes();

        Map<String, Integer> statisticsMaps = new HashMap<>();
        Map<String, List<Node>> nodeMaps = new HashMap<>();

        for(Node node : nodeList) {
            System.out.println("--------------");
            System.out.println(node.getId()+" : " + node.getVersionMessage().getBestBlockHeight() + " : " + node.getVersionMessage().getBestBlockHash());
            System.out.println("--------------");
            String hash = node.getVersionMessage().getBestBlockHash();

            Integer statistics = statisticsMaps.get(hash);

            if(statistics == null) {
                statisticsMaps.put(hash, 0);
            }

            statisticsMaps.put(hash, statisticsMaps.get(hash) + 1);

            List<Node> nodes = nodeMaps.get(hash);
            if(nodes == null) {
                nodes = new ArrayList<>();
                nodeMaps.put(hash, nodes);
            }
            nodes.add(node);
        }

        //max number
        int max = 0;
        long bestHeight = 0;
        String bestHash = null;
        List<Node> nodes = null;

        for (Map.Entry<String, Integer> entry : statisticsMaps.entrySet()) {
            int count = entry.getValue();
            String hash = entry.getKey();

            List<Node> tempNodes = nodeMaps.get(hash);
            long height = tempNodes.get(0).getVersionMessage().getBestBlockHeight();

            if(count > max || (count == max && bestHeight < height)) {
                max = count;
                bestHash = hash;
                bestHeight = height;
                nodes = tempNodes;
            }
        }

        if(nodes == null || nodes.size() == 0) {
            throw new NulsRuntimeException(ErrorCode.NET_NODE_NOT_FOUND);
        }

        return new NetworkNewestBlockInfos(bestHeight, bestHash, nodes);
    }

    private void waitNetworkNotChange() throws NulsRuntimeException {
        //等待10秒内节点没有变化（一般是增长），则开始同步
        //Wait for no change in the node within 10 seconds (usually growth), then start synchronization

        int nodeSize = networkService.getAvailableNodes().size();

        long now = TimeService.currentTimeMillis();
        long timeout = 10000L;
        while(true) {
            int newNodeSize = networkService.getAvailableNodes().size();
            if(newNodeSize > nodeSize) {
                now = TimeService.currentTimeMillis();
                nodeSize = newNodeSize;
            }

            if(TimeService.currentTimeMillis() - now >= timeout) {
                break;
            }
            try {
                Thread.sleep(500l);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }

        //check node size again
        nodeSize = networkService.getAvailableNodes().size();
        if(nodeSize < PocConsensusConstant.ALIVE_MIN_NODE_COUNT) {
            downloadStatus = DownloadStatus.WAIT;
            return;
        }
        downloadStatus = DownloadStatus.READY;
    }

    private boolean checkNetworkAndStatus() {
        //监控网络，如果掉线，重新连线后重新同步
        //Monitor the network, if it is dropped, reconnect after reconnecting
        Collection<Node> nodes = networkService.getAvailableNodes();
        if (nodes == null || nodes.size() == 0 || nodes.size() < PocConsensusConstant.ALIVE_MIN_NODE_COUNT) {
            return false;
        }

        if(downloadStatus != DownloadStatus.WAIT) {
            return false;
        }
        return true;
    }

    public boolean startup() {
        threadPool = TaskManager.createScheduledThreadPool(1,
                new NulsThreadFactory(NulsConstant.MODULE_ID_CONSENSUS, "data-synchronize"));
        threadPool.scheduleAtFixedRate(INSTANCE, 0,1, TimeUnit.SECONDS);
        return true;
    }

    public boolean shutdown() {
        threadPool.shutdownNow();
        downloadStatus = DownloadStatus.STOPPED;
        return true;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }
}

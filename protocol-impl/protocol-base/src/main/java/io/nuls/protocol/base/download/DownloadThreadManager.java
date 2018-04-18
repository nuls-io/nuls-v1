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
import io.nuls.consensus.poc.protocol.service.DownloadService;
import io.nuls.consensus.poc.protocol.service.SystemService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.NulsThreadFactory;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.service.impl.QueueService;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadThreadManager implements Callable<Boolean> {

    private DownloadUtils downloadUtils = new DownloadUtils();
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);

    private NetworkNewestBlockInfos newestInfos;
    private QueueService<Block> blockQueue;
    private String queueName;

    private int maxDowncount = 100;

    public DownloadThreadManager(NetworkNewestBlockInfos newestInfos, QueueService<Block> blockQueue, String queueName) {
        this.newestInfos = newestInfos;
        this.blockQueue = blockQueue;
        this.queueName = queueName;
    }

    @Override
    public Boolean call() throws Exception {

        System.out.println("============================");
        System.out.println(newestInfos);
        System.out.println("============================");

//        if(1==1)return true;

        try {
            boolean isContinue = checkFirstBlock();

            if (!isContinue) {
                return true;
            }
        } catch (NulsRuntimeException e) {
            return false;
        }

        List<Node> nodes = newestInfos.getNodes();
        String netBestHash = newestInfos.getNetBestHash();
        long netBestHeight = newestInfos.getNetBestHeight();

        Block localBestBlock = blockService.getBestBlock();
        String localBestHash = localBestBlock.getHeader().getHash().getDigestHex();
        long localBestHeight = localBestBlock.getHeader().getHeight();

        ThreadPoolExecutor executor = TaskManager.createThreadPool(nodes.size(), 0,
                new NulsThreadFactory(NulsConstant.MODULE_ID_CONSENSUS, "download-thread"));

        List<FutureTask<ResultMessage>> futures = new ArrayList<>();

        long totalCount = netBestHeight - localBestHeight;

        long laveCount = totalCount;

        long downCount = (long) Math.ceil((double) totalCount / (maxDowncount * nodes.size()));

        for(long i = 0 ; i < downCount ; i++) {

            long startHeight = (localBestHeight + 1) + i * maxDowncount * nodes.size();

            for(int j = 0 ; j < nodes.size() ; j ++) {

                long start = startHeight + j * maxDowncount;
                int size = maxDowncount;

                boolean isEnd = false;
                if(start + size >= netBestHeight) {
                    size = (int) (netBestHeight - start) + 1;
                    isEnd = true;
                }

                DownloadThread downloadThread = new DownloadThread(localBestHash, netBestHash, start, size, nodes.get(j));

                FutureTask<ResultMessage> downloadThreadFuture = new FutureTask<ResultMessage>(downloadThread);
                executor.execute(new Thread(downloadThreadFuture));

                futures.add(downloadThreadFuture);

                if(isEnd) {
                    break;
                }
            }
        }

        for (FutureTask<ResultMessage> task : futures) {
            ResultMessage result = null;
            try {
                result = task.get();
            } catch (Exception e) {
                Log.error(e);
            }
            List<Block> blockList = null;

            if(result == null || (blockList = result.getBlockList()) == null || blockList.size() == 0) {
                blockList = retryDownload(executor, result);
            }

            if(blockList == null) {
                executor.shutdown();
                resetNetwork("attempts to download blocks from all available nodes failed");
                return true;
            }

            for(Block block : blockList) {
                blockQueue.offer(queueName, block);
            }
        }
        executor.shutdown();

        return true;
    }

    private List<Block> retryDownload(ThreadPoolExecutor executor, ResultMessage result) throws InterruptedException, ExecutionException {

        //try download to other nodes
        List<Node> otherNodes = new ArrayList<>();

        Node defultNode = result.getNode();

        for(Node node : newestInfos.getNodes()) {
            if(!node.getId().equals(defultNode.getId())) {
                otherNodes.add(node);
            }
        }

        for(Node node : otherNodes) {
            result.setNode(node);
            List<Block> blockList = downloadBlockFromNode(executor, result);
            if(blockList != null && blockList.size() > 0) {
                return blockList;
            }
        }

        //if fail , down again
        result.setNode(defultNode);

        return downloadBlockFromNode(executor, result);
    }

    private List<Block> downloadBlockFromNode(ThreadPoolExecutor executor, ResultMessage result) throws ExecutionException, InterruptedException {
        DownloadThread downloadThread = new DownloadThread(result.getStartHash(), result.getEndHash(), result.getStartHeight(), result.getSize(), result.getNode());

        FutureTask<ResultMessage> downloadThreadFuture = new FutureTask<ResultMessage>(downloadThread);
        executor.execute(new Thread(downloadThreadFuture));

        List<Block> blockList = null;
        try {
            blockList = downloadThreadFuture.get().getBlockList();
        } catch (Exception e) {
            Log.error(e);
        }
        return blockList;
    }

    private boolean checkFirstBlock() throws NulsException {

        Block localBestBlock = blockService.getBestBlock();

        if(localBestBlock.getHeader().getHeight() == 0 || (newestInfos.getNetBestHeight() == localBestBlock.getHeader().getHeight() &&
                newestInfos.getNetBestHash().equals(localBestBlock.getHeader().getHash().getDigestHex()))) {
            return true;
        }

        if(newestInfos.getNetBestHeight() < localBestBlock.getHeader().getHeight()) {
            if(DoubleUtils.div(newestInfos.getNodes().size(), networkService.getAvailableNodes().size(), 2) >= 0.8d && networkService.getAvailableNodes().size() >= networkService.getNetworkParam().maxOutCount()) {
                for (long i = localBestBlock.getHeader().getHeight(); i <= newestInfos.getNetBestHeight(); i--) {
                    blockService.rollbackBlock(localBestBlock.getHeader().getHash().getDigestHex());
                    localBestBlock = blockService.getBestBlock();
                }
            } else {
                resetNetwork("The local block is higher than the network block, the number of connected nodes is not enough to allow the local rollback, so reset");
                return false;
            }
        }

        //check need rollback
        checkRollback(localBestBlock, 0);

        return true;
    }

    private void checkRollback(Block localBestBlock, int rollbackCount) throws NulsException {

        if(rollbackCount >= 10) {
            resetNetwork("number of rollback blocks greater than 10 during download");
            return;
        }

        List<Node> nodes = newestInfos.getNodes();

        for(Node node : nodes) {
            Block remoteBlock = downloadUtils.getBlockByHash(localBestBlock.getHeader().getHash().getDigestHex(), node);
            if(remoteBlock != null && remoteBlock.getHeader().getHeight() == localBestBlock.getHeader().getHeight()) {
                return;
            }
        }

        if(newestInfos.getNodes().size() >= PocConsensusConstant.ALIVE_MIN_NODE_COUNT) {
            blockService.rollbackBlock(localBestBlock.getHeader().getHash().getDigestHex());
        } else {
            resetNetwork("the number of available nodes is insufficient for rollback blocks");
            return;
        }

        localBestBlock = blockService.getBestBlock();

        checkRollback(localBestBlock, rollbackCount + 1);
    }

    private void resetNetwork(String reason) {
        NulsContext.getServiceBean(SystemService.class).resetSystem(reason);
        throw new NulsRuntimeException(ErrorCode.FAILED);
    }

}

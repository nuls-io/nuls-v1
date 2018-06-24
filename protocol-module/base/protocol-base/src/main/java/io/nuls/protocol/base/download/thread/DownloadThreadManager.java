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
 *
 */

package io.nuls.protocol.base.download.thread;

import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.calc.DoubleUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.model.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.base.download.entity.NetworkNewestBlockInfos;
import io.nuls.protocol.base.download.entity.ResultMessage;
import io.nuls.protocol.base.download.utils.DownloadUtils;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.service.BlockService;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ln
 * @date 2018/4/8
 */
public class DownloadThreadManager implements Callable<Boolean> {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);
    private NulsThreadFactory factory = new NulsThreadFactory(ProtocolConstant.MODULE_ID_PROTOCOL, "download");

    private NetworkNewestBlockInfos newestInfos;
    private Queue<Block> blockQueue;
    private String queueName;

    private int maxDowncount = 10;

    public DownloadThreadManager(NetworkNewestBlockInfos newestInfos, Queue<Block> blockQueue) {
        this.newestInfos = newestInfos;
        this.blockQueue = blockQueue;
        this.queueName = queueName;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            boolean isContinue = checkFirstBlock();

            if (!isContinue) {
                return true;
            }
        } catch (NulsRuntimeException e) {
            return false;
        }

        List<Node> nodes = newestInfos.getNodes();
        NulsDigestData netBestHash = newestInfos.getNetBestHash();
        long netBestHeight = newestInfos.getNetBestHeight();
        Block localBestBlock = blockService.getBestBlock().getData();
        NulsDigestData localBestHash = localBestBlock.getHeader().getHash();
        long localBestHeight = localBestBlock.getHeader().getHeight();

        ThreadPoolExecutor executor = TaskManager.createThreadPool(nodes.size(), 0,
                new NulsThreadFactory(ProtocolConstant.MODULE_ID_PROTOCOL, "download-thread"));

        List<FutureTask<ResultMessage>> futures = new ArrayList<>();

        long totalCount = netBestHeight - localBestHeight;

        long laveCount = totalCount;

        long downCount = (long) Math.ceil((double) totalCount / (maxDowncount * nodes.size()));

        for (long i = 0; i < downCount; i++) {

            long startHeight = (localBestHeight + 1) + i * maxDowncount * nodes.size();

            for (int j = 0; j < nodes.size(); j++) {

                long start = startHeight + j * maxDowncount;
                int size = maxDowncount;

                boolean isEnd = false;
                if (start + size >= netBestHeight) {
                    size = (int) (netBestHeight - start) + 1;
                    isEnd = true;
                }

                DownloadThread downloadThread = new DownloadThread(localBestHash, netBestHash, start, size, nodes.get(j));

                FutureTask<ResultMessage> downloadThreadFuture = new FutureTask<>(downloadThread);

                executor.execute(factory.newThread(downloadThreadFuture));

                futures.add(downloadThreadFuture);

                if (isEnd) {
                    break;
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

                if (result == null || (blockList = result.getBlockList()) == null || blockList.size() == 0) {
                    blockList = retryDownload(executor, result);
                }

                if (blockList == null) {
                    executor.shutdown();
                    resetNetwork("attempts to download blocks from all available nodes failed");
                    return true;
                }

                for (Block block : blockList) {
                    blockQueue.offer(block);
                }
            }
            futures.clear();
        }

        executor.shutdown();

        return true;
    }

    private List<Block> retryDownload(ThreadPoolExecutor executor, ResultMessage result) throws InterruptedException, ExecutionException {

        //try download to other nodes
        List<Node> otherNodes = new ArrayList<>();

        Node defultNode = result.getNode();

        for (Node node : newestInfos.getNodes()) {
            if (!node.getId().equals(defultNode.getId())) {
                otherNodes.add(node);
            }
        }

        for (Node node : otherNodes) {
            result.setNode(node);
            List<Block> blockList = downloadBlockFromNode(executor, result);
            if (blockList != null && blockList.size() > 0) {
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

        Block localBestBlock = blockService.getBestBlock().getData();

        if (localBestBlock.getHeader().getHeight() == 0 || (newestInfos.getNetBestHeight() == localBestBlock.getHeader().getHeight() &&
                newestInfos.getNetBestHash().equals(localBestBlock.getHeader().getHash()))) {
            return true;
        }

        if (newestInfos.getNetBestHeight() < localBestBlock.getHeader().getHeight()) {
            BlockHeader header = blockService.getBlockHeader(newestInfos.getNetBestHash()).getData();

            if (null == header && networkService.getAvailableNodes().size() >= networkService.getNetworkParam().getMaxOutCount() && DoubleUtils.div(newestInfos.getNodes().size(), networkService.getAvailableNodes().size(), 2) >= 0.8d) {
                for (long i = localBestBlock.getHeader().getHeight(); i <= newestInfos.getNetBestHeight(); i--) {
                    consensusService.rollbackBlock(localBestBlock);
                    localBestBlock = blockService.getBestBlock().getData();
                }
            } else if (null == header) {
                resetNetwork("The local block is higher than the network block, the number of connected nodes is not enough to allow the local rollbackTx, so reset");
                return false;
            }
        } else {
            //check need rollbackTx
            checkRollback(localBestBlock, 0);
        }
        return true;
    }

    private void checkRollback(Block localBestBlock, int rollbackCount) throws NulsException {

        if (rollbackCount >= 10) {
//            resetNetwork("number of rollbackTx blocks greater than 10 during download");
            return;
        }

        List<Node> nodes = newestInfos.getNodes();
        //todo 这里逻辑感觉不完整
        for (Node node : nodes) {
            Block remoteBlock = DownloadUtils.getBlockByHash(localBestBlock.getHeader().getHash(), node);
            if (remoteBlock != null && remoteBlock.getHeader().getHeight() == localBestBlock.getHeader().getHeight()) {
                return;
            }
        }

        if (newestInfos.getNodes().size() > 0) {
            consensusService.rollbackBlock(localBestBlock);
        } else {
//            resetNetwork("the number of available nodes is insufficient for rollbackTx blocks");
            return;
        }

        localBestBlock = blockService.getBestBlock().getData();

        checkRollback(localBestBlock, rollbackCount + 1);
    }

    private void resetNetwork(String reason) {
        NulsContext.getServiceBean(NetworkService.class).reset();
        throw new NulsRuntimeException(KernelErrorCode.FAILED);
    }

}

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

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ln
 */
public class DownloadThreadManager implements Callable<Boolean> {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);

    private NetworkNewestBlockInfos newestInfos;
    private String queueName;

    private int maxDowncount = 10;

    public DownloadThreadManager(NetworkNewestBlockInfos newestInfos) {
        this.newestInfos = newestInfos;
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
        long netBestHeight = newestInfos.getNetBestHeight();
        long localBestHeight = blockService.getBestBlock().getData().getHeader().getHeight();
        RequestThread requestThread = new RequestThread(nodes, localBestHeight + 1, netBestHeight);
        CollectThread collectThread = CollectThread.initInstance(localBestHeight + 1, netBestHeight, requestThread);
        TaskManager.createAndRunThread(ProtocolConstant.MODULE_ID_PROTOCOL, "download-collect", collectThread);
        TaskManager.createAndRunThread(ProtocolConstant.MODULE_ID_PROTOCOL, "download-request", requestThread);
        return true;
    }


    private boolean checkFirstBlock() throws NulsException {

        Block localBestBlock = blockService.getBestBlock().getData();

        if (localBestBlock.getHeader().getHeight() == 0 || (newestInfos.getNetBestHeight() == localBestBlock.getHeader().getHeight() &&
                newestInfos.getNetBestHash().equals(localBestBlock.getHeader().getHash()))) {
            return true;
        }

        if (newestInfos.getNetBestHeight() < localBestBlock.getHeader().getHeight()) {
            BlockHeader header = blockService.getBlockHeader(newestInfos.getNetBestHash()).getData();

            if (null == header && networkService.getAvailableNodes().size() >= networkService.getNetworkParam().getMaxOutCount() && DoubleUtils.div(newestInfos.getNodes().size(), networkService.getAvailableNodes().size(), 2) >= 0.5d) {
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
            return checkRollback(localBestBlock, 0);
        }
        return true;
    }

    private boolean checkRollback(Block localBestBlock, int rollbackCount) throws NulsException {

        if (rollbackCount >= 10) {
//            resetNetwork("number of rollbackTx blocks greater than 10 during download");
            return false;
        }

        List<Node> nodes = newestInfos.getNodes();

        long localHeight = localBestBlock.getHeader().getHeight();
        NulsDigestData localBestHash = localBestBlock.getHeader().getHash();

        for (Node node : nodes) {
            Block block = DownloadUtils.getBlockByHash(localBestHash, node);
            if (block != null && localHeight == block.getHeader().getHeight()) {
                return true;
            }
        }

        if (newestInfos.getNodes().size() > 0) {
            consensusService.rollbackBlock(localBestBlock);
        } else {
//            resetNetwork("the number of available nodes is insufficient for rollbackTx blocks");
            return false;
        }

        localBestBlock = blockService.getBestBlock().getData();

        return checkRollback(localBestBlock, rollbackCount + 1);

    }

    private void resetNetwork(String reason) {
        NulsContext.getServiceBean(NetworkService.class).reset();
        throw new NulsRuntimeException(KernelErrorCode.FAILED);
    }

}

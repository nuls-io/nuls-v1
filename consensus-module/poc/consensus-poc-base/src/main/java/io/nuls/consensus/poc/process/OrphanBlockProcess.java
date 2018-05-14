/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.provider.OrphanBlockProvider;
import io.nuls.core.tools.log.ChainLog;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.service.DownloadService;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author ln
 * @date 2018/4/14
 */
public class OrphanBlockProcess implements Runnable {

    private DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);

    private ChainManager chainManager;
    private OrphanBlockProvider orphanBlockProvider;

    private boolean running = true;

    public OrphanBlockProcess(ChainManager chainManager, OrphanBlockProvider orphanBlockProvider) {
        this.chainManager = chainManager;
        this.orphanBlockProvider = orphanBlockProvider;
    }

    public void start() {
        TaskManager.createAndRunThread(ConsensusConstant.MODULE_ID_CONSENSUS, "process-orphan-thread", this);
    }

    @Override
    public void run() {
        while(running) {
            try {
                process();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void process() throws IOException {
        BlockContainer blockContainer;
        while ((blockContainer = orphanBlockProvider.get()) != null) {
            process(blockContainer);
        }
    }

    public void process(BlockContainer blockContainer) throws IOException {

        Block block = blockContainer.getBlock();

        // 只处理本地误差配置的块个数以内的孤块
        long bestBlockHeight = chainManager.getBestBlockHeight();

        if (Math.abs(bestBlockHeight - block.getHeader().getHeight()) > PocConsensusConstant.MAX_ISOLATED_BLOCK_COUNT) {
            return;
        }

        // Because it is not possible to ensure that there will be repeated reception, priority is given to
        // 因为不能确保是否会有重复收到的情况，所以在此优先去重
        boolean hasExist = checkHasExist(block.getHeader().getHash());
        if (hasExist) {
            return;
        }

        ChainLog.debug("process isolated block, bestblockheight:{}, isolated {} - {}", bestBlockHeight, block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());

        // Checks if the current orphaned block is connected to an existing orphaned chain
        // 检查当前孤立块是否和已经存在的孤立链连接
        boolean success = chainManager.checkIsBeforeOrphanChainAndAdd(block);

        ChainLog.debug("checkIsBeforeIsolatedChainAndAdd: {} , block {} - {}", success, block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());

        if (success) {
            // Connect to the current isolated chain and continue to find the previous block
            // 和当前的孤立链连接，继续寻找上一区块
            foundAndProcessPreviousBlock(blockContainer);
            return;
        }
        success = chainManager.checkIsAfterOrphanChainAndAdd(block);

        ChainLog.debug("checkIsAfterIsolatedChainAndAdd: {} , block {} - {}", success, block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());

        if (success) {
            // Successfully found and connected, no action required
            // 成功找到，并且连接上了，接下来不需要做任何操作
            return;
        }

        // Not found, then create a new isolated chain, then find the previous block
        // 没有找到，那么新建一条孤立链，接着寻找上一个区块
        chainManager.newOrphanChain(block);

        ChainLog.debug("new a isolated chain , block {} - {}", success, block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());

        foundAndProcessPreviousBlock(blockContainer);
    }

    /*
     * Check the existence of this block from the forked chain and the isolated chain
     * 从分叉链和孤立链中检查，是否存在该区块
     */
    private boolean checkHasExist(NulsDigestData blockHash) {

        for (ChainContainer chainContainer : chainManager.getOrphanChains()) {
            for (BlockHeader header : chainContainer.getChain().getBlockHeaderList()) {
                if (header.getHash().equals(blockHash)) {
                    return true;
                }
            }
        }

        for (ChainContainer chainContainer : chainManager.getChains()) {
            for (BlockHeader header : chainContainer.getChain().getBlockHeaderList()) {
                if (header.getHash().equals(blockHash)) {
                    return true;
                }
            }
        }

        List<BlockHeader> masterChainBlockHeaderList = chainManager.getMasterChain().getChain().getBlockHeaderList();
        int size = (int) (masterChainBlockHeaderList.size() - PocConsensusConstant.MAX_ISOLATED_BLOCK_COUNT * 1.05);
        if(size < 0) {
            size = 0;
        }
        for (int i = masterChainBlockHeaderList.size() - 1 ; i >= size ; i--) {
            if(blockHash.equals(masterChainBlockHeaderList.get(i).getHash())) {
                return true;
            }
        }

        return false;
    }

    private void foundAndProcessPreviousBlock(BlockContainer blockContainer) {

        BlockHeader blockHeader = blockContainer.getBlock().getHeader();

        // Determine whether the previous block already exists. If it already exists, it will not be downloaded.
        // 判断上一区块是否已经存在，如果已经存在则不下载
        boolean hasExist = checkHasExist(blockHeader.getPreHash());
        if (hasExist) {
            return;
        }

        Block preBlock = downloadService.downloadBlock(blockHeader.getPreHash(), blockContainer.getNode()).getData();

        if(preBlock != null) {
            ChainLog.debug("get pre block success {} - {}", preBlock.getHeader().getHeight(), preBlock.getHeader().getHash());
            orphanBlockProvider.addBlock(new BlockContainer(preBlock, blockContainer.getNode(), BlockContainerStatus.DOWNLOADING));
        } else {
            ChainLog.debug("get pre block fail {} - {}", blockHeader.getHeight() - 1, blockHeader.getPreHash());

            //失败情况的处理，从其它所以可用的节点去获取，如果都不成功，那么就失败，包括本次失败的节点，再次获取一次
            for(Node node : networkService.getAvailableNodes()) {
                preBlock = downloadService.downloadBlock(blockHeader.getPreHash(), node).getData();
                if(preBlock != null) {
                    orphanBlockProvider.addBlock(new BlockContainer(preBlock, node, BlockContainerStatus.DOWNLOADING));
                    ChainLog.debug("get pre block retry success {} - {}", preBlock.getHeader().getHeight() - 1, preBlock.getHeader().getPreHash());
                    return;
                }
            }
            ChainLog.debug("get pre block complete failure {} - {}", blockHeader.getHeight() - 1, blockHeader.getPreHash());
        }
    }

    public void stop() {
        running = false;
    }
}

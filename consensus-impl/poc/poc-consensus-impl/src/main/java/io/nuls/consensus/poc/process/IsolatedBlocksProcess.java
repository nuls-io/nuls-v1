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

package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.constant.ConsensusConstant;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.provider.DownloadBlockProvider;
import io.nuls.core.utils.log.ChainLog;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.BlockHeader;
import io.nuls.protocol.model.NulsDigestData;

import java.io.IOException;
import java.util.List;

/**
 * Created by ln on 2018/4/14.
 */
public class IsolatedBlocksProcess {

    private ChainManager chainManager;
    private BlockProcess blockProcess;

    private DownloadBlockProvider downloadBlockProvider;

    public IsolatedBlocksProcess(ChainManager chainManager) {
        this.chainManager = chainManager;
    }

    public void process(BlockContainer blockContainer) throws IOException {

        Block block = blockContainer.getBlock();

        // 只处理本地误差配置的块个数以内的孤块

        long bestBlockHeight = chainManager.getBestBlockHeight();

        if (Math.abs(bestBlockHeight - block.getHeader().getHeight()) > ConsensusConstant.MAX_ISOLATED_BLOCK_COUNT) {
            return;
        }

        // Because it is not possible to ensure that there will be repeated reception, priority is given to
        // 因为不能确保是否会有重复收到的情况，所以在此优先去重
        boolean hasExist = checkHasExist(blockContainer);
        if (hasExist) {
            return;
        }

        ChainLog.debug("process isolated block, bestblockheight:{}, isolated {} - {}", bestBlockHeight, block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());

        // Checks if the current orphaned block is connected to an existing orphaned chain
        // 检查当前孤立块是否和已经存在的孤立链连接
        boolean success = chainManager.checkIsBeforeIsolatedChainAndAdd(block);

        ChainLog.debug("checkIsBeforeIsolatedChainAndAdd: {} , block {} - {}", success, block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());

        if (success) {
            // Connect to the current isolated chain and continue to find the previous block
            // 和当前的孤立链连接，继续寻找上一区块
            foundPreviousBlock(blockContainer);
            return;
        }
        success = chainManager.checkIsAfterIsolatedChainAndAdd(block);

        ChainLog.debug("checkIsAfterIsolatedChainAndAdd: {} , block {} - {}", success, block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());

        if (success) {
            // Successfully found and connected, no action required
            // 成功找到，并且连接上了，接下来不需要做任何操作
            return;
        }

        // Not found, then create a new isolated chain, then find the previous block
        // 没有找到，那么新建一条孤立链，接着寻找上一个区块
        chainManager.newIsolatedChain(block);

        ChainLog.debug("new a isolated chain , block {} - {}", success, block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());

        foundPreviousBlock(blockContainer);

    }

    private void foundPreviousBlock(BlockContainer blockContainer) {
        downloadBlockProvider.put(blockContainer);
    }


    /*
     * Check the existence of this block from the forked chain and the isolated chain
     * 从分叉链和孤立链中检查，是否存在该区块
     */
    private boolean checkHasExist(BlockContainer blockContainer) {
        NulsDigestData hash = blockContainer.getBlock().getHeader().getHash();

        for (ChainContainer chainContainer : chainManager.getIsolatedChains()) {
            for (BlockHeader header : chainContainer.getChain().getBlockHeaderList()) {
                if (header.getHash().equals(hash)) {
                    return true;
                }
            }
        }

        for (ChainContainer chainContainer : chainManager.getChains()) {
            for (BlockHeader header : chainContainer.getChain().getBlockHeaderList()) {
                if (header.getHash().equals(hash)) {
                    return true;
                }
            }
        }

        List<BlockHeader> masterChainBlockHeaderList = chainManager.getMasterChain().getChain().getBlockHeaderList();
        int size = (int) (masterChainBlockHeaderList.size() - ConsensusConstant.MAX_ISOLATED_BLOCK_COUNT * 1.1);
        if(size < 0) {
            size = 0;
        }
        for (int i = masterChainBlockHeaderList.size() - 1 ; i > size ; i--) {
            if(hash.equals(masterChainBlockHeaderList.get(i).getHash())) {
                return true;
            }
        }

        return false;
    }

    public void setBlockProcess(BlockProcess blockProcess) {
        this.blockProcess = blockProcess;
    }

    public void setDownloadBlockProvider(DownloadBlockProvider downloadBlockProvider) {
        this.downloadBlockProvider = downloadBlockProvider;
    }
}

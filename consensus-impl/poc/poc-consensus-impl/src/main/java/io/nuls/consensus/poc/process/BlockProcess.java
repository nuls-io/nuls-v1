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

import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.provider.ConsensusSystemProvider;
import io.nuls.consensus.poc.provider.IsolatedBlocksProvider;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.protocol.intf.BlockService;

import java.io.IOException;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class BlockProcess {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    private ChainManager chainManager;
    private IsolatedBlocksProvider isolatedBlocksProvider;

    public BlockProcess(ChainManager chainManager, IsolatedBlocksProvider isolatedBlocksProvider) {
        this.chainManager = chainManager;
        this.isolatedBlocksProvider = isolatedBlocksProvider;
    }

    public boolean addBlock(BlockContainer blockContainer) throws IOException {
        boolean isDownload = blockContainer.getStatus() == BlockContainerStatus.DOWNLOADING;
        Block block = blockContainer.getBlock();

        if(chainManager.getMasterChain().verifyAndAddBlock(block, isDownload)) {
            boolean success = false;
            try {
                success = blockService.saveBlock(block);
            } catch(Exception e) {
                Log.error("save block error : " + e.getMessage(), e);
            }
            if(success) {
                NulsContext.getInstance().setBestBlock(block);
                // 转发区块
                forwardingBlock(blockContainer);
                return true;
            } else {
                chainManager.getMasterChain().rollback();
                // if save block fail, put in temporary cache
                // TODO
            }
        } else {
            // Failed to block directly in the download
            // 下载中验证失败的区块直接丢弃
            if(isDownload && ConsensusStatus.RUNNING != ConsensusSystemProvider.getConsensusStatus()) {
                return false;
            }
            ChainContainer needVerifyChain = checkAndGetForkChain(block);
            if(needVerifyChain == null) {
                isolatedBlocksProvider.addBlock(blockContainer);
            } else if(!chainManager.getChains().contains(needVerifyChain)) {
                chainManager.getChains().add(needVerifyChain);
            }
        }
        return false;
    }

    private void forwardingBlock(BlockContainer blockContainer) {
        //TODO
    }

    private ChainContainer checkAndGetForkChain(Block block) {
        // check the preHash is in the master chain
        ChainContainer forkChain = getForkChain(chainManager.getMasterChain(), block);
        if(forkChain != null) {
            return forkChain;
        }

        // check the preHash is in the waitVerifyChainList
        for(ChainContainer waitVerifyChain : chainManager.getChains()) {
            forkChain = getForkChain(waitVerifyChain, block);
            if(forkChain != null) {
                break;
            }
        }
        return forkChain;
    }

    private ChainContainer getForkChain(ChainContainer chainContainer, Block block) {
        BlockHeader blockHeader = chainContainer.getChain().getEndBlockHeader();

        NulsDigestData preHash = block.getHeader().getPreHash();
        if(blockHeader.getHash().equals(preHash)) {
            chainContainer.getChain().setEndBlockHeader(block.getHeader());
            chainContainer.getChain().getBlockHeaderList().add(block.getHeader());
            chainContainer.getChain().getBlockList().add(block);
            return chainContainer;
        }

        List<BlockHeader> headerList = chainContainer.getChain().getBlockHeaderList();

        for(BlockHeader header : headerList) {
            if(header.getHash().equals(preHash)) {
                ChainContainer forkChain = new ChainContainer();
                forkChain.getChain().getBlockList().add(block);
                forkChain.getChain().getBlockHeaderList().add(block.getHeader());
                forkChain.getChain().setStartBlockHeader(block.getHeader());
                forkChain.getChain().setEndBlockHeader(block.getHeader());
                return forkChain;
            }
        }
        return null;
    }
}

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

import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.constant.ConsensusConstant;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.protocol.event.notice.PackedBlockNotice;
import io.nuls.consensus.poc.protocol.service.BlockService;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.consensus.poc.provider.ConsensusSystemProvider;
import io.nuls.consensus.poc.provider.IsolatedBlocksProvider;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.ChainLog;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.SmallBlockEvent;
import io.nuls.protocol.model.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class BlockProcess {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);

    private ChainManager chainManager;
    private IsolatedBlocksProvider isolatedBlocksProvider;
    private TxMemoryPool txMemoryPool;

    public BlockProcess(ChainManager chainManager, IsolatedBlocksProvider isolatedBlocksProvider, TxMemoryPool txMemoryPool) {
        this.chainManager = chainManager;
        this.isolatedBlocksProvider = isolatedBlocksProvider;
        this.txMemoryPool = txMemoryPool;
    }

    public boolean addBlock(BlockContainer blockContainer) throws IOException {
        boolean isDownload = blockContainer.getStatus() == BlockContainerStatus.DOWNLOADING;
        Block block = blockContainer.getBlock();

        // Discard future blocks
        // 丢弃掉未来时间的区块
        if(TimeService.currentTimeMillis() + ConsensusConstant.DISCARD_FUTURE_BLOCKS_TIME < block.getHeader().getTime()) {
            return false;
        }

        if(chainManager.getMasterChain().verifyAndAddBlock(block, isDownload)) {
            boolean success = false;
            try {
                success = blockService.saveBlock(block);
            } catch(Exception e) {
                Log.error("save block error : " + e.getMessage(), e);
            }
            if(success) {
                NulsContext.getInstance().setBestBlock(block);
                //remove tx from memory pool
                removeTxFromMemoryPool(block);
                // 转发区块
                forwardingBlock(blockContainer);
                return true;
            } else {
                chainManager.getMasterChain().rollback(block);
                NulsContext.getInstance().setBestBlock(chainManager.getBestBlock());

                Log.error("save block fail : " + block.getHeader().getHeight() + " , isDownload : " + isDownload);
            }
        } else {
            // Failed to block directly in the download
            // 下载中验证失败的区块直接丢弃
            if(isDownload && ConsensusStatus.RUNNING != ConsensusSystemProvider.getConsensusStatus()) {
                return false;
            }

            boolean hasFoundForkChain = checkAndAddForkChain(block);
            if(!hasFoundForkChain) {

                ChainLog.debug("add block {} - {} in queue", block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());

                isolatedBlocksProvider.addBlock(blockContainer);
            }
        }
        return false;
    }

    private void removeTxFromMemoryPool(Block block) {
        for(Transaction tx : block.getTxs()) {
            txMemoryPool.remove(tx.getHash().getDigestHex());
        }
    }

    private void forwardingBlock(BlockContainer blockContainer) {
        if(blockContainer.getStatus() == BlockContainerStatus.DOWNLOADING) {
            return;
        }
        if(blockContainer.getNode() == null) {
            return;
        }
        SmallBlockEvent event = new SmallBlockEvent();
        SmallBlock smallBlock = ConsensusTool.getSmallBlock(blockContainer.getBlock());
        event.setEventBody(smallBlock);
        eventBroadcaster.broadcastHashAndCacheAysn(event, blockContainer.getNode().getId());
    }

    private boolean checkAndAddForkChain(Block block) {
        // check the preHash is in the master chain
        boolean hasFoundForkChain = checkForkChainFromMasterChain(block);
        if(hasFoundForkChain) {
            return hasFoundForkChain;
        }
        return checkForkChainFromForkChains(block);
    }

    private boolean checkForkChainFromMasterChain(Block block) {

        BlockHeader blockHeader = block.getHeader();

        Chain masterChain = chainManager.getMasterChain().getChain();
        List<BlockHeader> headerList = masterChain.getBlockHeaderList();

        for(int i = headerList.size() - 1 ; i >= 0 ; i--) {
            BlockHeader header = headerList.get(i);

            if(header.getHash().equals(blockHeader.getHash())) {
                // found a same block , return true
               return true;
            } else if(header.getHash().equals(blockHeader.getPreHash())) {

                Chain newForkChain = new Chain();

                newForkChain.getBlockList().add(block);
                newForkChain.getBlockHeaderList().add(block.getHeader());

                newForkChain.setStartBlockHeader(block.getHeader());
                newForkChain.setEndBlockHeader(block.getHeader());

                chainManager.getChains().add(new ChainContainer(newForkChain));
                return true;
            }

            if(header.getHeight() < blockHeader.getHeight()) {
                break;
            }
        }
        return false;
    }

    private boolean checkForkChainFromForkChains(Block block) {

        BlockHeader blockHeader = block.getHeader();
        NulsDigestData preHash = blockHeader.getPreHash();

        // check the preHash is in the waitVerifyChainList
        for(ChainContainer chainContainer : chainManager.getChains()) {

            Chain forkChain = chainContainer.getChain();
            List<BlockHeader> headerList = forkChain.getBlockHeaderList();

            for(int i = headerList.size() - 1 ; i >= 0 ; i--) {
                BlockHeader header = headerList.get(i);

                if(header.getHash().equals(blockHeader.getHash())) {
                    // found a same block , return true
                    return true;
                } else if(header.getHash().equals(preHash)) {

                    // Check whether it is forked or connected. If it is a connection, add it.
                    // 检查是分叉还是连接，如果是连接，则加上即可
                    if(i == headerList.size() - 1) {
                        chainContainer.getChain().setEndBlockHeader(block.getHeader());
                        chainContainer.getChain().getBlockHeaderList().add(block.getHeader());
                        chainContainer.getChain().getBlockList().add(block);
                        return true;
                    }

                    // The block is again forked in the forked chain
                    // 该块是在分叉链中再次进行的分叉
                    List<Block> blockList = forkChain.getBlockList();

                    Chain newForkChain = new Chain();

                    newForkChain.getBlockList().addAll(blockList.subList(0, i));
                    newForkChain.getBlockHeaderList().addAll(headerList.subList(0, i));

                    newForkChain.getBlockList().add(block);
                    newForkChain.getBlockHeaderList().add(block.getHeader());

                    newForkChain.setStartBlockHeader(forkChain.getStartBlockHeader());
                    newForkChain.setEndBlockHeader(block.getHeader());

                    chainManager.getChains().add(new ChainContainer(newForkChain));
                    return true;
                } else if(header.getHeight() < blockHeader.getHeight()) {
                    break;
                }
            }
        }
        return false;
    }

}

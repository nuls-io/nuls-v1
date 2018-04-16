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

import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.ChainLog;
import io.nuls.core.utils.log.Log;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.poc.service.intf.ConsensusService;
import io.nuls.protocol.intf.BlockService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class ChainProcess {

    private ChainManager chainManager;

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);

    public ChainProcess(ChainManager chainManager) {
        this.chainManager = chainManager;
    }

    public boolean process() throws IOException, NulsException {

        if(consensusService.getConsensusStatus().ordinal() < ConsensusStatus.RUNNING.ordinal()) {
            return false;
        }

        // Monitor the status of the isolated chain, if it is available, join the verification chain
        // 监控孤立链的状态，如果有可连接的，则加入验证链里面
        monitorIsolatedChains();

        long newestBlockHeight = chainManager.getBestBlockHeight();

        ChainContainer newChain = null;

        Iterator<ChainContainer> iterator = chainManager.getChains().iterator();
        while(iterator.hasNext()) {
            ChainContainer forkChain = iterator.next();
            if(forkChain.getChain() == null || forkChain.getChain().getStartBlockHeader() == null || forkChain.getChain().getEndBlockHeader() == null) {
                iterator.remove();
                continue;
            }
            long newChainHeight = forkChain.getChain().getEndBlockHeader().getHeight();
            if(newChainHeight > newestBlockHeight + 3) {
                if(newChain == null || newChain.getChain().getEndBlockHeader().getHeight() < newestBlockHeight) {
                    newChain = forkChain;
                }
            }
        }

        if(newChain != null) {

            ChainLog.debug("discover the fork chain {} : start {} - {} , end {} - {} , exceed the master {} - {} - {}, start verify the fork chian", newChain.getChain().getId(), newChain.getChain().getStartBlockHeader().getHeight(), newChain.getChain().getStartBlockHeader().getHash(), newChain.getChain().getEndBlockHeader().getHeight(), newChain.getChain().getEndBlockHeader().getHash(), chainManager.getMasterChain().getChain().getId(), chainManager.getBestBlockHeight(), chainManager.getBestBlock().getHeader().getHash());

            ChainContainer resultChain = verifyNewChain(newChain);

            if(resultChain == null) {
                ChainLog.debug("verify the fork chain fail {} remove it", newChain.getChain().getId());

                chainManager.getChains().remove(newChain);
            } else {
                //Verify pass, try to switch chain
                //验证通过，尝试切换链
                boolean success = changeChain(resultChain, newChain);
                if(success) {
                    chainManager.getChains().remove(newChain);
                }

                ChainLog.debug("verify the fork chain {} success, change master chain result : {} , new master chain is {} : {} - {}", newChain.getChain().getId(), success, chainManager.getBestBlock().getHeader().getHeight(), chainManager.getBestBlock().getHeader().getHash());

            }
        }

        //clear
        clearExpiredChain();

        return true;
    }

    /**
     * Monitor the isolated chain, if there is a connection with the main chain or the forked chain, the merged chain
     *
     * 监控孤立链，如果有和主链或者分叉链连上的情况，则合并链
     */
    private void monitorIsolatedChains() {
        List<ChainContainer> isolatedChains = chainManager.getIsolatedChains();

        Iterator<ChainContainer> iterator = isolatedChains.iterator();
        while(iterator.hasNext()) {
            ChainContainer isolatedChain = iterator.next();
            if(checkIsolatedChainHasConnection(isolatedChain)) {
                iterator.remove();
            }
        }
    }

    private boolean checkIsolatedChainHasConnection(ChainContainer isolatedChain) {
        //判断该孤立链是否和主链相连
        BlockHeader startBlockHeader = isolatedChain.getChain().getStartBlockHeader();

        List<BlockHeader> blockHeaderList = chainManager.getMasterChain().getChain().getBlockHeaderList();

        for(int i = 0 ; i < blockHeaderList.size() ; i++) {
            BlockHeader header = blockHeaderList.get(i);
            if(startBlockHeader.getPreHash().equals(header.getHash()) && startBlockHeader.getHeight() == header.getHeight() + 1) {
                //yes connectioned
                isolatedChain.getChain().setPreChainId(chainManager.getMasterChain().getChain().getId());

                List<Block> isolatedChainBlockList = isolatedChain.getChain().getBlockList();

                isolatedChain.getChain().setStartBlockHeader(isolatedChainBlockList.get(0).getHeader());
                isolatedChain.getChain().setEndBlockHeader(isolatedChainBlockList.get(isolatedChainBlockList.size() - 1).getHeader());
                chainManager.getChains().add(isolatedChain);

                ChainLog.debug("discover the IsolatedChain {} : start {} - {} , end {} - {} , connection the master chain of {} - {} - {}, move into the fork chians", isolatedChain.getChain().getId(), startBlockHeader.getHeight(), startBlockHeader.getHash().getDigestHex(), isolatedChain.getChain().getEndBlockHeader().getHeight(), isolatedChain.getChain().getEndBlockHeader().getHash(), chainManager.getMasterChain().getChain().getId(), chainManager.getMasterChain().getChain().getBestBlock().getHeader().getHeight(), chainManager.getMasterChain().getChain().getBestBlock().getHeader().getHash());

                return true;
            }
        }

        //判断该孤链是否和待验证的分叉链相连
        for(ChainContainer forkChain : chainManager.getChains()) {

            Chain chain = forkChain.getChain();
            blockHeaderList = chain.getBlockHeaderList();

            for(int i = 0 ; i < blockHeaderList.size() ; i++) {
                BlockHeader header = blockHeaderList.get(i);
                if(startBlockHeader.getPreHash().equals(header.getHash()) && startBlockHeader.getHeight() == header.getHeight() + 1) {
                    //yes connectioned
                    isolatedChain.getChain().setPreChainId(chain.getPreChainId());
                    isolatedChain.getChain().setStartBlockHeader(chain.getStartBlockHeader());

                    int index = blockHeaderList.indexOf(header);

                    isolatedChain.getChain().getBlockHeaderList().addAll(blockHeaderList.subList(0, index));
                    isolatedChain.getChain().getBlockList().addAll(chain.getBlockList().subList(0, index));

                    chainManager.getChains().add(isolatedChain);

                    ChainLog.debug("discover the IsolatedChain {} : start {} - {} , end {} - {} , connection the fork chain of : start {} - {} , end {} - {}, move into the fork chians", isolatedChain.getChain().getId(), startBlockHeader.getHeight(), startBlockHeader.getHash().getDigestHex(), isolatedChain.getChain().getEndBlockHeader().getHeight(), isolatedChain.getChain().getEndBlockHeader().getHash(), chainManager.getMasterChain().getChain().getId(), chain.getStartBlockHeader().getHeight(), chain.getStartBlockHeader().getHash(), chain.getEndBlockHeader().getHeight(), chain.getEndBlockHeader().getHash());

                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Verify the block header information of the new chain, and if they all pass, start switching
     * However, in the case that both are passed, it may also fail because the transaction is not verified.
     * The transaction cannot be verified here because the data has not been rolled back
     *
     * 验证新链的区块头信息，如果都通过，才开始切换
     * 但是都通过的情况下，也有可能失败，因为交易是没有经过验证的
     * 这里不能同时验证交易，因为数据没有回滚
     */
    private ChainContainer verifyNewChain(ChainContainer needVerifyChain) {
        //Verify the new chain, combined with the current latest chain, to get the status of the branch node
        //验证新的链，结合当前最新的链，获取到分叉节点时的状态
        ChainContainer forkChain = chainManager.getMasterChain().getBeforeTheForkChain(needVerifyChain);

        //Combined with the new bifurcated block chain, combine and verify one by one
        //结合新分叉的块链， 逐个组合并验证
        for (Block forkBlock : needVerifyChain.getChain().getBlockList()) {
            boolean success = forkChain.verifyAndAddBlock(forkBlock, true);
            if(!success) {
                return null;
            }
        }
        return forkChain;
    }

    /*
     * Switching the master chain to a new chain and verifying the block header before the switch is legal, so only the transactions in the block need to be verified here.
     * In order to ensure the correctness of the transaction verification, you first need to roll back all blocks after the fork of the main chain, and then the new chain will start to go into storage.
     * If the verification fails during the warehousing process, it means that the transaction in the block is illegal, then the new connection that proves the need to switch is not trusted.
     * Once the new chain is not trusted, you need to add the previously rolled back block back
     * This method needs to be synchronized with the add block method
     *
     * 把master链切换成新的链，切换之前已经做过区块头的验证，都是合法的，所以这里只需要验证区块里面的交易即可
     * 为保证交易验证的正确性，首先需要回滚掉主链分叉点之后的所有区块，然后新链开始入库，入库过程中会做验证
     * 如果入库过程中验证失败，说明是区块里面的交易不合法，那么证明需要切换的新连是不可信的
     * 一旦出现新链不可信的情况，则需要把之前回滚掉的区块再添加回去
     * 本方法需要和添加区块方法同步
     */
    private boolean changeChain(ChainContainer newMasterChain, ChainContainer originalForkChain) throws NulsException, IOException {

        if(newMasterChain == null || originalForkChain == null) {
            return false;
        }

        //Now the master chain, the forked chain after the switch, needs to be put into the list of chains to be verified.
        //现在的主链，在切换之后的分叉链，需要放入待验证链列表里面
        ChainContainer oldChain = chainManager.getMasterChain().getAfterTheForkChain(originalForkChain);

        //rollback
        List<Block> rollbackBlockList = oldChain.getChain().getBlockList();

        ChainLog.debug("rollback the master chain , need rollback block count is {}, master chain is {} : {} - {} , storage best block : {} - {}", rollbackBlockList.size(), chainManager.getMasterChain().getChain().getId(), chainManager.getBestBlock().getHeader().getHeight(), chainManager.getBestBlock().getHeader().getHash(), blockService.getBestBlock().getHeader().getHeight(), blockService.getBestBlock().getHeader().getHash());

        //Need descending order
        //需要降序排列
        Collections.reverse(rollbackBlockList);

        List<Block> rollbackList = new ArrayList<>();

        for(Block rollbackBlock : rollbackBlockList) {
            try {
                boolean success = blockService.rollbackBlock(rollbackBlock);
                if(success) {
                    rollbackList.add(rollbackBlock);
                }
            } catch (Exception e) {
                Collections.reverse(rollbackList);
                for(Block block : rollbackList) {
                    try {
                        blockService.saveBlock(block);
                    } catch (Exception ex) {
                        Log.error("Rollback failed, failed to save block during recovery", ex);
                        break;
                    }
                }
                Log.error("Rollback failed during switch chain, skip this chain", e);
                return false;
            }
        }

        ChainLog.debug("rollback complete, success count is {} , now storage best block : {} - {}", rollbackList.size(), blockService.getBestBlock().getHeader().getHeight(), blockService.getBestBlock().getHeader().getHash());

        //add new block

        List<Block> addBlockList = originalForkChain.getChain().getBlockList();

        boolean changeSuccess = true;

        List<Block> successList = new ArrayList<>();

        //Need to sort in ascending order, the default is
        //需要升序排列，默认就是
        for(Block newBlock : addBlockList) {
            try {
                boolean success = blockService.saveBlock(newBlock);
                if(success) {
                    successList.add(newBlock);
                } else {
                    changeSuccess = false;
                    break;
                }
            } catch (Exception e) {
                Log.info("change fork chain error at save block, ", e);
                changeSuccess = false;
                break;
            }
        }

        ChainLog.debug("add new blocks complete, result {}, success count is {} , now storage best block : {} - {}", changeSuccess, successList.size(), blockService.getBestBlock().getHeader().getHeight(), blockService.getBestBlock().getHeader().getHash());

        if(changeSuccess) {
            chainManager.setMasterChain(newMasterChain);
            newMasterChain.getRoundList().clear();
            newMasterChain.resetRound(false);
            NulsContext.getInstance().setBestBlock(newMasterChain.getBestBlock());

            if(oldChain.getChain().getBlockList().size() > 0) {
                chainManager.getChains().add(oldChain);
            }
        } else {
            //Fallback status
            //回退状态
            Collections.reverse(successList);
            for(Block rollBlock : successList) {
                blockService.rollbackBlock(rollBlock);
            }

            Collections.reverse(rollbackBlockList);
            for(Block addBlock : rollbackBlockList) {
                blockService.saveBlock(addBlock);
            }
        }
        return changeSuccess;
    }

    private void clearExpiredChain() {
        //TODO
    }
}

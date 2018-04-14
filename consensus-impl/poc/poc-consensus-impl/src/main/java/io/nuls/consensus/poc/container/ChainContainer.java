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

package io.nuls.consensus.poc.container;

import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.protocol.base.entity.member.Agent;
import io.nuls.protocol.base.entity.tx.PocJoinConsensusTransaction;
import io.nuls.protocol.base.entity.tx.RegisterAgentTransaction;
import io.nuls.protocol.entity.Consensus;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class ChainContainer implements Cloneable {

    private Chain chain;

    private List<MeetingRound> roundList;

    public ChainContainer() {}

    public ChainContainer(Chain chain) {
        this.chain = chain;
    }

    public boolean addBlock(Block block) {
        //TODO
        return true;
    }

    public boolean verifyBlock(Block block) {
        return verifyBlock(block, false);
    }


    public boolean verifyBlock(Block block, boolean isDownload) {
        //TODO
        return true;
    }

    public boolean verifyAndAddBlock(Block block, boolean isDownload) {
        boolean success = verifyBlock(block, isDownload);
        if(success) {
            success = addBlock(block);
        }
        return success;
    }

    public boolean rollback() {

        List<Block> blockList = chain.getBlockList();

        if(blockList == null || blockList.size() == 0) {
            return false;
        }

        Block rollbackBlock = blockList.get(blockList.size() - 1);
        blockList.remove(rollbackBlock);

        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        chain.setEndBlockHeader(blockHeaderList.get(blockHeaderList.size() - 2));
        BlockHeader rollbackBlockHeader = blockHeaderList.get(blockHeaderList.size() - 1);
        blockHeaderList.remove(rollbackBlockHeader);

        //TODO update txs
        List<Consensus> agentList = chain.getAgentList();

        List<Transaction> txs = rollbackBlock.getTxs();
        for(Transaction tx : txs) {
            int txType = tx.getType();
            if(txType == TransactionConstant.TX_TYPE_REGISTER_AGENT) {

                //注册代理交易
                RegisterAgentTransaction registerAgentTx = (RegisterAgentTransaction) tx;
                Consensus<Agent> ca = registerAgentTx.getTxData();
                Agent agent = ca.getExtend();

                Iterator<Consensus> it = agentList.iterator();
                while(it.hasNext()) {
                    Consensus tempCa = it.next();
                    if(tempCa.getHash().equals(ca.getHash())) {
                        it.remove();
                        break;
                    }
                }

            } else if(txType == TransactionConstant.TX_TYPE_JOIN_CONSENSUS) {
                PocJoinConsensusTransaction joinConsensusTx = (PocJoinConsensusTransaction) tx;

            } else if(txType == TransactionConstant.TX_TYPE_EXIT_CONSENSUS) {

            } else if(txType == TransactionConstant.TX_TYPE_YELLOW_PUNISH) {

            } else if(txType == TransactionConstant.TX_TYPE_RED_PUNISH) {

            }
        }

        return true;
    }

    private void resetRound() {
        //TODO
    }

    /**
     *
     * Get the state of the complete chain after the combination of a chain and the current chain bifurcation point, that is, first obtain the bifurcation point between the bifurcation chain and the current chain.
     * Then create a brand new chain, copy all the states before the bifurcation point of the main chain to the brand new chain
     *
     * 获取一条链与当前链分叉点组合之后的完整链的状态，也就是，先获取到分叉链与当前链的分叉点，
     * 然后创建一条全新的链，把主链分叉点之前的所有状态复制到全新的链
     *
     * @param chainContainer
     * @return ChainContainer
     */
    public ChainContainer getBeforeTheForkChain(ChainContainer chainContainer) {

        ChainContainer newChain = null;
        try {
            newChain = (ChainContainer) this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        // Bifurcation
        // 分叉点
        BlockHeader pointBlockHeader = chainContainer.getChain().getStartBlockHeader();

        List<Block> blockList = newChain.getChain().getBlockList();
        for(int i = blockList.size() - 1 ; i >= 0; i--) {
            Block block = blockList.get(i);
            if(pointBlockHeader.getPreHash().equals(block.getHeader().getHash())) {
                break;
            }
            newChain.rollback();
        }
        newChain.resetRound();

        return newChain;
    }

    /**
     *
     * Get the block information of the current chain and branch chain after the cross point and combine them into a new branch chain
     *
     * 获取当前链与分叉链对比分叉点之后的区块信息，组合成一个新的分叉链
     *
     * @param chainContainer
     * @return ChainContainer
     */
    public ChainContainer getAfterTheForkChain(ChainContainer chainContainer) {

        // Bifurcation
        // 分叉点
        BlockHeader pointBlockHeader = chainContainer.getChain().getStartBlockHeader();

        Chain chain = new Chain();

        List<Block> blockList = getChain().getBlockList();
        List<BlockHeader> blockHeaderList = getChain().getBlockHeaderList();

        boolean canAdd = false;
        for(int i = 0 ; i < blockList.size() ; i++) {

            BlockHeader blockHeader = blockHeaderList.get(i);
            if(pointBlockHeader.getPreHash().equals(blockHeader.getHash())) {
                canAdd = true;
                if(i + 1 < blockHeaderList.size()) {
                    chain.setStartBlockHeader(blockHeaderList.get(i + 1));
                    chain.setPreChainId(chainContainer.getChain().getId());
                    chain.setEndBlockHeader(getChain().getEndBlockHeader());
                }
                continue;
            }
            if(canAdd) {
                Block block = blockList.get(i);
                chain.getBlockList().add(block);
                chain.getBlockHeaderList().add(blockHeader);
            }
        }
        return new ChainContainer(chain);
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public List<MeetingRound> getRoundList() {
        return roundList;
    }

    public void setRoundList(List<MeetingRound> roundList) {
        this.roundList = roundList;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof  ChainContainer)) {
            return false;
        }
        ChainContainer other = (ChainContainer) obj;
        if(other.getChain() == null || this.chain == null) {
            return false;
        }
        return other.getChain().getId().equals(this.chain.getId());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

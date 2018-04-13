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

package io.nuls.consensus.poc.entity;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.protocol.entity.Consensus;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class Chain implements Serializable {

    private String id;
    private String preChainId;
    private BlockHeader startBlockHeader;
    private BlockHeader endBlockHeader;
    private List<BlockHeader> blockHeaderList;
    private List<Block> blockList;
    private List<Consensus> agentList;
    private List<Consensus> depositList;
    private List<Consensus> allRedCardList;
    private List<Consensus> yellowCardList;


    public void setId(String id) {
        this.id = id;
    }

    public void setPreChainId(String preChainId) {
        this.preChainId = preChainId;
    }

    public void setStartBlockHeader(BlockHeader startBlockHeader) {
        this.startBlockHeader = startBlockHeader;
    }

    public void setEndBlockHeader(BlockHeader endBlockHeader) {
        this.endBlockHeader = endBlockHeader;
    }

    public void setBlockHeaderList(List<BlockHeader> blockHeaderList) {
        this.blockHeaderList = blockHeaderList;
    }

    public String getId() {
        return id;
    }

    public String getPreChainId() {
        return preChainId;
    }

    public BlockHeader getStartBlockHeader() {
        return startBlockHeader;
    }

    public BlockHeader getEndBlockHeader() {
        return endBlockHeader;
    }

    public List<BlockHeader> getBlockHeaderList() {
        return blockHeaderList;
    }

    public List<Block> getBlockList() {
        return blockList;
    }

    public List<Consensus> getAgentList() {
        return agentList;
    }

    public List<Consensus> getDepositList() {
        return depositList;
    }

    public List<Consensus> getAllRedCardList() {
        return allRedCardList;
    }

    public List<Consensus> getYellowCardList() {
        return yellowCardList;
    }

    public void setBlockList(List<Block> blockList) {
        this.blockList = blockList;
    }

    public void setAgentList(List<Consensus> agentList) {
        this.agentList = agentList;
    }

    public void setDepositList(List<Consensus> depositList) {
        this.depositList = depositList;
    }

    public void setAllRedCardList(List<Consensus> allRedCardList) {
        this.allRedCardList = allRedCardList;
    }

    public void setYellowCardList(List<Consensus> yellowCardList) {
        this.yellowCardList = yellowCardList;
    }
}

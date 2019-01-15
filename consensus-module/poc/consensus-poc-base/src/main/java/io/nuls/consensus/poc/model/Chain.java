/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.consensus.poc.model;

import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;

import java.util.*;

/**
 * @author ln
 */
public class Chain implements Cloneable {

    private String id;
    private String preChainId;
    private BlockHeader startBlockHeader;
    private BlockHeader endBlockHeader;
    private List<BlockHeader> blockHeaderList;
    private List<Block> blockList;
    private List<Agent> agentList;
    private List<Deposit> depositList;
    private List<PunishLogPo> yellowPunishList;
    private List<PunishLogPo> redPunishList;

    public Chain() {
        blockHeaderList = new ArrayList<>();
        blockList = new ArrayList<>();
        id = StringUtils.getNewUUID();
    }


    public void addBlock(Block block) {
        endBlockHeader = block.getHeader();
        blockHeaderList.add(block.getHeader());
        if (blockHeaderList.size() > PocConsensusConstant.MAX_BLOCK_HEADER_COUNT) {
            blockHeaderList.remove(0);
        }
        blockList.add(block);
        if (blockList.size() > PocConsensusConstant.MAX_ISOLATED_BLOCK_COUNT) {
            blockList.remove(0);
        }
        if (null == startBlockHeader) {
            this.startBlockHeader = block.getHeader();
        }
    }

    public BlockHeader rollbackBlock() {
        blockList.remove(blockList.size() - 1);
        BlockHeader header;
        if (blockHeaderList.size() == 1) {
            header = blockHeaderList.remove(blockHeaderList.size() - 1);
            this.startBlockHeader = null;
            this.endBlockHeader = null;
        } else {
            BlockHeader preHeader = blockHeaderList.get(blockHeaderList.size() - 2);
            header = blockHeaderList.remove(blockHeaderList.size() - 1);
            if (header.getPreHash().equals(preHeader.getHash())) {
                this.endBlockHeader = preHeader;
            } else {
                this.endBlockHeader = blockHeaderList.get(blockHeaderList.size() - 1);
            }
        }
        return header;
    }

    public List<BlockHeader> getAllBlockHeaderList() {
        return blockHeaderList;
    }

    public List<Block> getAllBlockList() {
        return blockList;
    }

    public BlockHeader getStartBlockHeader() {
        return startBlockHeader;
    }

    public BlockHeader getEndBlockHeader() {
        return this.endBlockHeader;
    }

    public List<Agent> getAgentList() {
        return agentList;
    }

    public void setAgentList(List<Agent> agentList) {
        this.agentList = agentList;
    }

    public List<Deposit> getDepositList() {
        return depositList;
    }

    public void setDepositList(List<Deposit> depositList) {
        this.depositList = depositList;
    }

    public List<PunishLogPo> getYellowPunishList() {
        return yellowPunishList;
    }

    public void setYellowPunishList(List<PunishLogPo> yellowPunishList) {
        this.yellowPunishList = yellowPunishList;
    }

    public List<PunishLogPo> getRedPunishList() {
        return redPunishList;
    }

    public void setRedPunishList(List<PunishLogPo> redPunishList) {
        this.redPunishList = redPunishList;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPreChainId(String preChainId) {
        this.preChainId = preChainId;
    }

    public String getId() {
        return id;
    }

    public String getPreChainId() {
        return preChainId;
    }

    public Agent getAgentByAddress(byte[] address) {
        for (Agent agent : agentList) {
            if (agent.getDelHeight() > 0) {
                continue;
            }
            if (ArraysTool.arrayEquals(agent.getAgentAddress(), address)) {
                return agent;
            }
        }
        return null;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Block getBestBlock() {
        if (blockList == null || blockList.size() == 0) {
            return null;
        }
        return blockList.get(blockList.size() - 1);
    }

    public void initData(BlockHeader startHeader, List<BlockHeader> headerList, List<Block> blockList) {
        this.startBlockHeader = startHeader;
        this.endBlockHeader = headerList.get(headerList.size() - 1);
        this.blockHeaderList = headerList;
        this.blockList = blockList;
    }

    public void initData(Block block) {
        this.startBlockHeader = block.getHeader();
        this.endBlockHeader = block.getHeader();
        this.blockHeaderList.add(block.getHeader());
        this.blockList.add(block);
    }

    public void addPreBlock(Block block) {
        this.startBlockHeader = block.getHeader();
        this.blockHeaderList.add(0, block.getHeader());
        this.blockList.add(0, block);
    }
}

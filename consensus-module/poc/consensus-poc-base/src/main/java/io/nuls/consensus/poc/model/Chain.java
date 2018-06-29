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

package io.nuls.consensus.poc.model;

import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public List<Agent> getAgentList() {
        return agentList;
    }

    public List<Deposit> getDepositList() {
        return depositList;
    }

    public void setBlockList(List<Block> blockList) {
        this.blockList = blockList;
    }

    public void setAgentList(List<Agent> agentList) {
        this.agentList = agentList;
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
}

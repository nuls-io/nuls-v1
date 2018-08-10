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

package io.nuls.consensus.poc.rpc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.util.PoConvertUtil;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.utils.AddressTool;

import java.io.UnsupportedEncodingException;

/**
 * @author Niels
 */
public class AgentDTO {

    public AgentDTO(Agent agent,String alias) {
        this.agentHash = agent.getTxHash().getDigestHex();
        this.agentAddress = AddressTool.getStringAddressByBytes(agent.getAgentAddress());
        this.packingAddress = AddressTool.getStringAddressByBytes(agent.getPackingAddress());
        this.rewardAddress = AddressTool.getStringAddressByBytes(agent.getRewardAddress());
        this.deposit = agent.getDeposit().getValue();
        this.commissionRate = agent.getCommissionRate();
        this.agentName = alias;
        this.agentId = PoConvertUtil.getAgentId(agent.getTxHash());
        this.time = agent.getTime();
        this.blockHeight = agent.getBlockHeight();
        this.delHeight = agent.getDelHeight();
        this.status = agent.getStatus();
        this.creditVal = agent.getCreditVal();
        this.totalDeposit = agent.getTotalDeposit();
        this.txHash = agent.getTxHash().getDigestHex();
        this.memberCount = agent.getMemberCount();
    }

    private String agentHash;

    private String agentAddress;

    private String packingAddress;

    private String rewardAddress;

    private long deposit;

    private double commissionRate;

    private String agentName;

    private String agentId;

    @JsonIgnore
    private String introduction;

    private long time;
    private long blockHeight = -1L;
    private long delHeight = -1L;
    private int status;
    private double creditVal;
    private long totalDeposit;
    private String txHash;
    private final int memberCount;

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = packingAddress;
    }

    public String getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(String rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    public long getDeposit() {
        return deposit;
    }

    public void setDeposit(long deposit) {
        this.deposit = deposit;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(long delHeight) {
        this.delHeight = delHeight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getCreditVal() {
        return creditVal;
    }

    public void setCreditVal(double creditVal) {
        this.creditVal = creditVal;
    }

    public long getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(long totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}

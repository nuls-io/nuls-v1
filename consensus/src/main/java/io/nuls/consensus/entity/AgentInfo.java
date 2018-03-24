/**
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
package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.Na;

/**
 * author Facjas
 * date 2018/3/20.
 */
public class AgentInfo {
    //节点名称
    private String agentName;
    //    代理地址
    private String agentAddress;

    private String agentAddressAlias ="小节";
    //    状态
    private int status;
    //    自己的保证金
    private Na owndeposit;
    //    所有委托之和
    private Na totalDeposit;
    //    代理佣金
    private double commissionRate;
    //    信用值
    private double creditRatio;
    //    委托数量
    private int memberCount;
    //    节点介绍
    private String introduction;
    //    累计奖励
    private Na reward;

    private long packedCount;

    private long startTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getPackedCount() {
        return packedCount;
    }

    public void setPackedCount(long packedCount) {
        this.packedCount = packedCount;
    }

    public String getAgentAddressAlias() {
        return agentAddressAlias;
    }

    public void setAgentAddressAlias(String agentAddressAlias) {
        this.agentAddressAlias = agentAddressAlias;
    }

    public Na getReward() {
        return reward;
    }

    public void setReward(Na reward) {
        this.reward = reward;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Na getOwndeposit() {
        return owndeposit;
    }

    public void setOwndeposit(Na owndeposit) {
        this.owndeposit = owndeposit;
    }

    public Na getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(Na totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public double getCreditRatio() {
        return creditRatio;
    }

    public void setCreditRatio(double creditRatio) {
        this.creditRatio = creditRatio;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}

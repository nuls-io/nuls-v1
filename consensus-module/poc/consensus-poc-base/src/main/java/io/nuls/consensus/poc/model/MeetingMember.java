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

import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.Sha256Hash;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.SerializeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/25
 */
public class MeetingMember implements Comparable<MeetingMember> {
    private long roundIndex;
    private long roundStartTime;
    private byte[] agentAddress;
    private byte[] packingAddress;
    private byte[] rewardAddress;
    private NulsDigestData agentHash;
    /**
     * Starting from 1
     */
    private int packingIndexOfRound;
    private double creditVal;
    private Agent agent;
    private List<Deposit> depositList = new ArrayList<>();
    private Na totalDeposit = Na.ZERO;
    private Na ownDeposit = Na.ZERO;
    private double commissionRate;
    private String sortValue;
    private long packStartTime;
    private long packEndTime;

    public Na getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(Na totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public String getSortValue() {
        if (this.sortValue == null) {
            byte[] hash = ArraysTool.concatenate(packingAddress, SerializeUtils.uint64ToByteArray(roundStartTime));
            sortValue = Sha256Hash.twiceOf(hash).toString();
        }
        return sortValue;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
        this.sortValue = null;
    }

    public int getPackingIndexOfRound() {
        return packingIndexOfRound;
    }

    public void setPackingIndexOfRound(int packingIndexOfRound) {
        this.packingIndexOfRound = packingIndexOfRound;
    }

    public long getPackStartTime() {
        return packStartTime;
    }

    public void setPackStartTime(long packStartTime) {
        this.packStartTime = packStartTime;
    }

    public long getPackEndTime() {
        return packEndTime;
    }

    public void setPackEndTime(long packEndTime) {
        this.packEndTime = packEndTime;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public double getRealCreditVal() {
        return creditVal;
    }

    public double getCalcCreditVal() {
        return creditVal < 0d ? 0D : this.creditVal;
    }

    public void setCreditVal(double creditVal) {
        this.creditVal = creditVal;
    }

    public Na getOwnDeposit() {
        return ownDeposit;
    }

    public void setOwnDeposit(Na ownDeposit) {
        this.ownDeposit = ownDeposit;
    }

    @Override
    public int compareTo(MeetingMember o2) {
        return this.getSortValue().compareTo(o2.getSortValue());
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public byte[] getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(byte[] agentAddress) {
        this.agentAddress = agentAddress;
    }

    public byte[] getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
    }

    public NulsDigestData getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(NulsDigestData agentHash) {
        this.agentHash = agentHash;
    }

    public double getCreditVal() {
        return creditVal;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public List<Deposit> getDepositList() {
        return depositList;
    }

    public void setDepositList(List<Deposit> depositList) {
        this.depositList = depositList;
    }

    public void setSortValue(String sortValue) {
        this.sortValue = sortValue;
    }

    public byte[] getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(byte[] rewardAddress) {
        this.rewardAddress = rewardAddress;
    }
}

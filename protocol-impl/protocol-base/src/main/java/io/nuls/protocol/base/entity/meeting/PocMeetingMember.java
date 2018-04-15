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
package io.nuls.protocol.base.entity.meeting;

import io.nuls.account.entity.Address;
import io.nuls.protocol.base.constant.PocConsensusConstant;
import io.nuls.protocol.base.entity.member.Agent;
import io.nuls.protocol.base.entity.member.Deposit;
import io.nuls.protocol.entity.Consensus;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.crypto.Sha256Hash;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/25
 */
public class PocMeetingMember implements Comparable<PocMeetingMember> {
    private long roundIndex;
    private long roundStartTime;
    private String agentAddress;
    private String packingAddress;
    private String agentHash;
    /**
     * Starting from 1
     */
    private int packingIndexOfRound;
    private double creditVal;
    private String sortValue;
    private Consensus<Agent> agentConsensus;
    private List<Consensus<Deposit>> depositList = new ArrayList<>();
    private Na totalDeposit = Na.ZERO;
    private Na ownDeposit = Na.ZERO;
    private double commissionRate;

    public Consensus<Agent> getAgentConsensus() {
        return agentConsensus;
    }

    public void setAgentConsensus(Consensus<Agent> agentConsensus) {
        this.agentConsensus = agentConsensus;
    }

    public Na getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(Na totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public List<Consensus<Deposit>> getDepositList() {
        return depositList;
    }

    public void setDepositList(List<Consensus<Deposit>> depositList) {
        this.depositList = depositList;
    }

    public String getSortValue() {
        if (this.sortValue == null) {
            String hashHex = new Address(this.getPackingAddress()).hashHex() + roundStartTime;
            sortValue = Sha256Hash.twiceOf((hashHex).getBytes()).toString();
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

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

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

    public int getPackingIndexOfRound() {
        return packingIndexOfRound;
    }

    public void setPackingIndexOfRound(int packingIndexOfRound) {
        this.packingIndexOfRound = packingIndexOfRound;
    }

    public long getPackStartTime() {
        long packTime = PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000 * (this.getPackingIndexOfRound() - 1) + roundStartTime;
        return packTime;
    }

    public long getPackEndTime() {
        long packTime = PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000 * this.getPackingIndexOfRound() + roundStartTime;
        return packTime;
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
    public int compareTo(PocMeetingMember o2) {
        return this.getSortValue().compareTo(o2.getSortValue());
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }
}

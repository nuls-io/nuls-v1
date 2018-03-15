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
package io.nuls.consensus.entity.meeting;

import io.nuls.account.entity.Address;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.crypto.Sha256Hash;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/25
 */
public class PocMeetingMember implements Comparable<PocMeetingMember> {
    private long roundIndex;
    private long roundStartTime;
    private int indexOfRound;
    private String address;
    private String packerAddress;
    private long packTime;

    private double creditVal;

    private String sortValue;
    private Consensus<Agent> agentConsensus;
    private List<Consensus<Delegate>> delegateList;
    private Na totolEntrustDeposit = Na.ZERO;

    public Consensus<Agent> getAgentConsensus() {
        return agentConsensus;
    }

    public void setAgentConsensus(Consensus<Agent> agentConsensus) {
        this.agentConsensus = agentConsensus;
    }

    public Na getTotolEntrustDeposit() {
        return totolEntrustDeposit;
    }

    public void setTotolEntrustDeposit(Na totolEntrustDeposit) {
        this.totolEntrustDeposit = totolEntrustDeposit;
    }

    public List<Consensus<Delegate>> getDelegateList() {
        return delegateList;
    }

    public void setDelegateList(List<Consensus<Delegate>> delegateList) {
        this.delegateList = delegateList;
    }

    public String getSortValue() {
        return sortValue;
    }

    public void setSortValue(String sortValue) {
        this.sortValue = sortValue;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPackerAddress() {
        return packerAddress;
    }

    public void setPackerAddress(String packerAddress) {
        this.packerAddress = packerAddress;
    }

    public int getIndexOfRound() {
        return indexOfRound;
    }

    public void setIndexOfRound(int indexOfRound) {
        this.indexOfRound = indexOfRound;
    }

    public long getPackTime() {
        return packTime;
    }

    public void setPackTime(long packTime) {
        this.packTime = packTime;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public double getCreditVal() {
        return creditVal;
    }

    public void setCreditVal(double creditVal) {
        this.creditVal = creditVal;
    }

    @Override
    public int compareTo(PocMeetingMember o2) {
        if (this.getSortValue() == null) {
            String hashHex = new Address(this.getAddress()).hashHex();
            this.setSortValue(Sha256Hash.twiceOf((roundStartTime + hashHex).getBytes()).toString());
        }
        if (o2.getSortValue() == null) {
            String hashHex = new Address(o2.getAddress()).hashHex();
            o2.setSortValue(Sha256Hash.twiceOf((o2.getRoundStartTime() + hashHex).getBytes()).toString());
        }
        return this.getSortValue().compareTo(o2.getSortValue());
    }

    public void calcDeposit() {
        Na totolEntrustDeposit = Na.ZERO;
        if(delegateList==null){
            return;
        }
        for(Consensus<Delegate> dc:delegateList){
            totolEntrustDeposit .add(dc.getExtend().getDeposit());
        }
        this.totolEntrustDeposit = totolEntrustDeposit;
    }
}

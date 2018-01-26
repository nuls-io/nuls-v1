/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.entity.meeting;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/25
 */
public class PocMeetingRound {


    public PocMeetingRound(PocMeetingRound previousRound) {
        this.previousRound = previousRound;
    }

    private PocMeetingRound previousRound;
    private Na totalDeposit;
    private Na agentTotalDeposit;
    private long index;
    private long startTime;
    private long endTime;
    private int memberCount;
    private List<PocMeetingMember> memberList;
    private Map<String, Integer> addressOrderMap = new HashMap<>();
    private ConsensusGroup consensusGroup;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public PocMeetingMember getMember(int order) {
        if (null == memberList || memberList.isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "consensus member list is empty");
        }
        return this.memberList.get(order);
    }

    public void setMemberList(List<PocMeetingMember> memberList) {
        this.memberList = memberList;
        if (null == memberList || memberList.isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "consensus member list is empty");
        }
        addressOrderMap.clear();
        for (int i = 0; i < memberList.size(); i++) {
            PocMeetingMember pmm = memberList.get(i);
            pmm.setIndexOfRound(i + 1);
            pmm.setPackTime(pmm.getRoundStartTime() + PocConsensusConstant.BLOCK_TIME_INTERVAL * 1000 * pmm.getIndexOfRound());
            addressOrderMap.put(pmm.getPackerAddress(), i);
        }
    }

    public int getOrder(String address) {
        Integer val = addressOrderMap.get(address);
        if (null == val) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "address not in consensus:" + address);
        }
        return val;
    }

    public PocMeetingMember getMember(String address) {
        int order = getOrder(address);
        return getMember(order);
    }

    public PocMeetingRound getPreviousRound() {
        return previousRound;
    }

    public void setPreviousRound(PocMeetingRound previousRound) {
        this.previousRound = previousRound;
    }

    public ConsensusGroup getConsensusGroup() {
        return consensusGroup;
    }

    public void setConsensusGroup(ConsensusGroup consensusGroup) {
        this.consensusGroup = consensusGroup;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public Na getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(Na totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public void setAgentTotalDeposit(Na agentTotalDeposit) {
        this.agentTotalDeposit = agentTotalDeposit;
    }

    public Na getAgentTotalDeposit() {
        return agentTotalDeposit;
    }

    public Integer indexOf(String address) {
        Integer index = addressOrderMap.get(address);
        if (index == null) {
            index = -1;
        }
        return index;
    }


}

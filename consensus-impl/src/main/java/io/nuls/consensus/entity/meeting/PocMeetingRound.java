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

import io.nuls.account.entity.Account;
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
    private Account localPacker;
    private Na totalDeposit;
    private long index;
    private long startTime;
    private int memberCount;
    private List<PocMeetingMember> memberList;
    private Map<String, Integer> addressOrderMap = new HashMap<>();
    private PocMeetingRound preRound;

    public PocMeetingRound getPreRound() {
        return preRound;
    }

    public void setPreRound(PocMeetingRound preRound) {
        this.preRound = preRound;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return startTime + memberCount * PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public PocMeetingMember getMember(int order) {
        if (order == 0) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "the parameter is wrong:memberOrder");
        }
        if (null == memberList || memberList.isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "consensus member list is empty");
        }
        return this.memberList.get(order-1);
    }

    public void setMemberList(List<PocMeetingMember> memberList) {
        this.memberList = memberList;
        if (null == memberList || memberList.isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "consensus member list is empty");
        }
        this.memberCount = memberList.size();
        addressOrderMap.clear();
        for (int i = 0; i < memberList.size(); i++) {
            PocMeetingMember pmm = memberList.get(i);
            pmm.setRoundIndex(this.getIndex());
            pmm.setRoundStartTime(this.getStartTime());
            pmm.setIndexOfRound(i + 1);
            addressOrderMap.put(pmm.getPackingAddress(), i+1);
        }
    }

    public Integer getOrder(String address) {
        Integer val = addressOrderMap.get(address);
        if (null == val) {
            return null;
        }
        return val;
    }

    public PocMeetingMember getMember(String address) {
        Integer order = getOrder(address);
        if (null == order) {
            return null;
        }
        return getMember(order);
    }

    public Account getLocalPacker() {
        return localPacker;
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

    public List<PocMeetingMember> getMemberList() {
        return memberList;
    }

    public void calcLocalPacker(List<Account> accountList) {
        for (Account account : accountList) {
            if (null != this.getOrder(account.getAddress().getBase58())) {
                this.localPacker = account;
                return;
            }
        }
    }
}

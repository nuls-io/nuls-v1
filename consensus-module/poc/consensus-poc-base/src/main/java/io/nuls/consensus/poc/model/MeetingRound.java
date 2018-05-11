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

import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;

import java.util.*;

/**
 * @author Niels
 * @date 2017/12/25
 */
public class MeetingRound {

    private Account localPacker;
    private double totalWeight;
    private long index;
    private long startTime;
    private long endTime;
    private int memberCount;
    private List<MeetingMember> memberList;
    private MeetingRound preRound;
    private MeetingMember myMember;

    public MeetingRound getPreRound() {
        return preRound;
    }

    public void setPreRound(MeetingRound preRound) {
        this.preRound = preRound;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void init(List<MeetingMember> memberList) {

        assert (startTime > 0L);

        this.memberList = memberList;
        if (null == memberList || memberList.isEmpty()) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_ERROR, "consensus member list is empty");
        }

        Collections.sort(memberList);

        this.memberCount = memberList.size();
        totalWeight = 0d;
        for (int i = 0; i < memberList.size(); i++) {
            MeetingMember member = memberList.get(i);
            member.setRoundIndex(this.getIndex());
            member.setRoundStartTime(this.getStartTime());
            member.setPackingIndexOfRound(i + 1);
            member.setPackStartTime(startTime + i * ConsensusConstant.BLOCK_TIME_INTERVAL_MILLIS);
            member.setPackEndTime(member.getPackStartTime() + ConsensusConstant.BLOCK_TIME_INTERVAL_MILLIS);
            totalWeight += member.getTotalDeposit().toDouble() + member.getOwnDeposit().toDouble();
        }
        endTime = startTime + memberCount * ConsensusConstant.BLOCK_TIME_INTERVAL_MILLIS;
    }

    public MeetingMember getMember(int order) {
        if (order == 0) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_ERROR, "the parameter is wrong:memberOrder");
        }
        if (null == memberList || memberList.isEmpty()) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_ERROR, "consensus member list is empty");
        }
        return this.memberList.get(order - 1);
    }

    public MeetingMember getMember(byte[] address) {
        for (MeetingMember member : memberList) {
            if (Arrays.equals(address, member.getPackingAddress())) {
                return member;
            }
        }
        return null;
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


    public double getTotalWeight() {
        return totalWeight;
    }

    public List<MeetingMember> getMemberList() {
        return memberList;
    }

    public MeetingMember getMyMember() {
        return myMember;
    }

    public void calcLocalPacker(List<Account> accountList) {
        for (Account account : accountList) {
            if(!account.isEncrypted()) {
                continue;
            }
            MeetingMember member = getMember(account.getAddress().getBase58Bytes());
            if (null != member) {
                this.localPacker = account;
                myMember = member;
                return;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (MeetingMember member : this.getMemberList()) {
            str.append(Address.fromHashs(member.getPackingAddress()).getBase58());
            str.append(" ,order:" + member.getPackingIndexOfRound());
            str.append(",packTime:" + new Date(member.getPackEndTime()));
            str.append(",creditVal:" + member.getRealCreditVal());
            str.append("\n");
        }
        if (null == this.getPreRound()) {
            return ("round:index:" + this.getIndex() + " , start:" + new Date(this.getStartTime())
                    + ", netTime:(" + new Date(TimeService.currentTimeMillis()).toString() + ") , totalWeight : " + totalWeight + " ,members:\n :" + str);
        } else {
            return ("round:index:" + this.getIndex() + " ,preIndex:" + this.getPreRound().getIndex() + " , start:" + new Date(this.getStartTime())
                    + ", netTime:(" + new Date(TimeService.currentTimeMillis()).toString() + ") , totalWeight : " + totalWeight + "  , members:\n :" + str);
        }
    }
}

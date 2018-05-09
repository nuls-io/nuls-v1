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

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.kernel.model.Na;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/7.
 */
public class MeetingRoundTest {

    private long roundStartTime = System.currentTimeMillis();

    @Test
    public void test() {
        List<MeetingMember> meetingMemberList = getMemberList();

        MeetingRound round = new MeetingRound();
        round.setStartTime(roundStartTime);
        round.init(meetingMemberList);

        assertEquals(round.getMemberCount(), meetingMemberList.size());
        assertEquals("error", 1010d, round.getTotalWeight(), 2);
        assertEquals(meetingMemberList.size() * ConsensusConstant.BLOCK_TIME_INTERVAL_MILLIS + roundStartTime , round.getEndTime());

        System.out.println(round.toString());
    }

    private List<MeetingMember> getMemberList() {
        List<MeetingMember> meetingMemberList = new ArrayList<>();

        for(int i = 0 ; i < 10 ; i ++) {
            MeetingMember member = new MeetingMember();
            member.setRoundStartTime(roundStartTime);
            member.setPackingAddress(new byte[i*3]);
            member.setOwnDeposit(Na.NA);
            member.setCreditVal(0.1 * (i + 1));
            member.setCommissionRate(0.2d);
            member.setTotalDeposit(Na.NA.multiply(100));
            member.setRoundIndex(1l);
            meetingMemberList.add(member);
        }

        return meetingMemberList;
    }
}
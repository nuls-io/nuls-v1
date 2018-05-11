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

package io.nuls.consensus.poc.manager;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.BaseChainTest;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.model.MeetingRound;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/7.
 */
public class RoundManagerTest extends BaseChainTest {

    private RoundManager roundManager;

    @Before
    public void initData() {
        initChain();

        roundManager = new RoundManager(chain);
    }

    @Test
    public void testAddRound() {
        assertNotNull(roundManager);
        assertNotNull(roundManager.getChain());

        assertEquals(0, roundManager.getRoundList().size());
        MeetingRound round = new MeetingRound();
        roundManager.addRound(round);

        assertEquals(1, roundManager.getRoundList().size());

        assertEquals(round, roundManager.getCurrentRound());

        MeetingRound round2 = new MeetingRound();
        roundManager.addRound(round2);

        assertEquals(2, roundManager.getRoundList().size());

        assertNotEquals(round, roundManager.getCurrentRound());

        assertEquals(round2, roundManager.getCurrentRound());
    }

    @Test
    public void testGetRoundByIndex() {
        assertNotNull(roundManager);
        assertNotNull(roundManager.getChain());

        long index = 1002L;

        assertEquals(0, roundManager.getRoundList().size());
        MeetingRound round = new MeetingRound();
        round.setIndex(index);
        roundManager.addRound(round);

        assertEquals(1, roundManager.getRoundList().size());

        MeetingRound round2 = roundManager.getRoundByIndex(index);
        assertNotNull(round2);
        assertEquals(round, round2);

    }

    @Test
    public void testClearRound() {
        MeetingRound round = new MeetingRound();
        round.setIndex(1l);
        roundManager.addRound(round);
        round = new MeetingRound();
        round.setIndex(2l);
        roundManager.addRound(round);
        round = new MeetingRound();
        round.setIndex(3l);
        roundManager.addRound(round);
        round = new MeetingRound();
        round.setIndex(4l);
        roundManager.addRound(round);
        round = new MeetingRound();
        round.setIndex(5l);
        roundManager.addRound(round);
        round = new MeetingRound();
        round.setIndex(6l);
        roundManager.addRound(round);
        round = new MeetingRound();
        round.setIndex(7l);
        roundManager.addRound(round);

        assertEquals(7, roundManager.getRoundList().size());
        assertEquals(7L, roundManager.getCurrentRound().getIndex());

        boolean success = roundManager.clearRound(3);
        assert(success);

        assertEquals(3, roundManager.getRoundList().size());
        assertEquals(7L, roundManager.getCurrentRound().getIndex());
    }

    @Test
    public void testInitRound() {
        assertNotNull(roundManager);
        assertNotNull(roundManager.getChain());

        Chain chain = roundManager.getChain();

        assertNotNull(chain.getEndBlockHeader());
        assert(chain.getBlockList().size() > 0);

        MeetingRound round = roundManager.initRound();

        assertNotNull(round);

        assertEquals(round.getIndex(), 2L);
        Assert.assertEquals(round.getStartTime(), ConsensusConstant.BLOCK_TIME_INTERVAL_MILLIS + 1L);

        MeetingRound round2 = roundManager.getNextRound(null, false);
        assertNotNull(round2);
        assertEquals(round.getIndex(), round2.getIndex());
        assertEquals(round.getStartTime(), round2.getStartTime());

        round2 = roundManager.getNextRound(null, true);
        assertNotNull(round2);
        assert(round.getIndex() < round2.getIndex());
        assert(round.getStartTime() < round2.getStartTime());
        assertEquals("", 0d, round2.getTotalWeight(), 2200000d);
    }
}
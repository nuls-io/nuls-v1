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

import io.nuls.consensus.entity.Agent;
import io.nuls.consensus.poc.customer.ConsensusAccountServiceImpl;
import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/7.
 */
public class RoundManagerTest {

    private RoundManager roundManager;

    @BeforeClass
    public static void init() {
        SpringLiteContext.putBean(ConsensusAccountServiceImpl.class,false);
    }

    @Before
    public void initData() {
        Chain chain = new Chain();
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

        initChain(chain);

        assertNotNull(chain.getEndBlockHeader());
        assert(chain.getBlockList().size() > 0);

        MeetingRound round = roundManager.initRound();

        assertNotNull(round);
    }

    private void initChain(Chain chain) {
        // new a block
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(0);
        blockHeader.setPreHash(NulsDigestData.calcDigestData("00000000000".getBytes()));
        blockHeader.setTime(1L);
        blockHeader.setTxCount(0);

        BlockRoundData roundData = new BlockRoundData();
        roundData.setConsensusMemberCount(1);
        roundData.setPackingIndexOfRound(1);
        roundData.setRoundIndex(1);
        roundData.setRoundStartTime(1L);
        blockHeader.setExtend(roundData.serialize());

        chain.setEndBlockHeader(blockHeader);

        Block block = new Block();
        block.setHeader(blockHeader);

        chain.getBlockList().add(block);

        List<Transaction<Agent>> agentList = new ArrayList<>();

        chain.setAgentList(agentList);

        chain.setDepositList(new ArrayList<>());
        chain.setYellowPunishList(new ArrayList<>());
        chain.setRedPunishList(new ArrayList<>());
    }
}
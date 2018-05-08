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

package io.nuls.consensus.poc.container;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.entity.Agent;
import io.nuls.consensus.entity.Deposit;
import io.nuls.consensus.poc.BaseChainTest;
import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.tx.JoinConsensusTransaction;
import io.nuls.consensus.tx.RegisterAgentTransaction;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/7.
 */
public class ChainContainerTest extends BaseChainTest {

    private ChainContainer chainContainer;

    @Before
    public void initData() {
        initChain();

        chainContainer = new ChainContainer(chain);

        chainContainer.getOrResetCurrentRound(false);
    }

    @Test
    public void testInit() {
        assertNotNull(chain);
        assertNotNull(chainContainer);

        assertNotNull(chainContainer.getCurrentRound());

        assertEquals(chainContainer.getCurrentRound().getMemberCount(), 1);
    }

    @Test
    public void testAddBlock() {
        Block bestBlock = chainContainer.getBestBlock();
        assertNotNull(bestBlock);

        Block newBlock = newBlock(bestBlock);

        boolean success = chainContainer.verifyBlock(newBlock);
        assertTrue(success);

        bestBlock = chainContainer.getBestBlock();
        assertEquals(bestBlock.getHeader().getHeight(), 0L);

        for(int i = 0 ; i < 100 ; i++) {

            newBlock = newBlock(bestBlock);

            success = chainContainer.verifyAndAddBlock(newBlock, false);
            assertTrue(success);

            bestBlock = chainContainer.getBestBlock();
            assertEquals(bestBlock.getHeader().getHeight(), 1L + i);
        }

        System.out.println();

    }

    @Test
    public void testAddAgent() {

        assertEquals(chainContainer.getCurrentRound().getMemberCount(), 1);

        Block bestBlock = chainContainer.getBestBlock();
        assertNotNull(bestBlock);

        Block newBlock = newBlock(bestBlock);

        addTx(newBlock);

        boolean success = chainContainer.verifyAndAddBlock(newBlock, false);
        assertTrue(success);

        bestBlock = chainContainer.getBestBlock();
        newBlock = newBlock(bestBlock);
        success = chainContainer.verifyAndAddBlock(newBlock, false);
        assertTrue(success);

        bestBlock = chainContainer.getBestBlock();
        newBlock = newBlock(bestBlock);
        success = chainContainer.verifyAndAddBlock(newBlock, false);
        assertTrue(success);

        bestBlock = chainContainer.getBestBlock();
        newBlock = newBlock(bestBlock);
        success = chainContainer.verifyAndAddBlock(newBlock, false);
        assertTrue(success);


        assertEquals(chainContainer.getCurrentRound().getMemberCount(), 2);

    }

    @Test
    public void testRollback() {
        testAddAgent();

        assertEquals(chainContainer.getCurrentRound().getMemberCount(), 2);
        for(int i = 3 ; i > 0 ; i--) {
            Block bestBlock = chainContainer.getBestBlock();
            boolean success = chainContainer.rollback(bestBlock);
            assert(success);
        }
        assertEquals(chainContainer.getCurrentRound().getMemberCount(), 1);
    }

    @Test
    public void testGetBeforeTheForkChain() {

        Block forkBlock = null;

        for(int i = 0 ; i < 20 ; i++) {

            Block bestBlock = chainContainer.getBestBlock();
            Block newBlock = newBlock(bestBlock);

            boolean success = chainContainer.verifyAndAddBlock(newBlock, false);
            assertTrue(success);

            bestBlock = chainContainer.getBestBlock();
            assertEquals(bestBlock.getHeader().getHeight(), 1L + i);

            if(i == 10) {
                forkBlock = bestBlock;
            }
        }

        Chain chain = new Chain();
        chain.setEndBlockHeader(forkBlock.getHeader());
        chain.setStartBlockHeader(forkBlock.getHeader());
        chain.getBlockList().add(forkBlock);

        Block newBlock = newBlock(forkBlock);
        chain.setEndBlockHeader(newBlock.getHeader());
        chain.getBlockList().add(newBlock);

        ChainContainer otherChainContainer = new ChainContainer(chain);
        ChainContainer newForkChainContainer = chainContainer.getBeforeTheForkChain(otherChainContainer);
        assertEquals(newForkChainContainer.getBestBlock().getHeader().getHeight(), 10L);

    }

    @Test
    public void testGetAfterTheForkChain() {
        Block forkBlock = null;

        for(int i = 0 ; i < 30 ; i++) {

            Block bestBlock = chainContainer.getBestBlock();
            Block newBlock = newBlock(bestBlock);

            boolean success = chainContainer.verifyAndAddBlock(newBlock, false);
            assertTrue(success);

            bestBlock = chainContainer.getBestBlock();
            assertEquals(bestBlock.getHeader().getHeight(), 1L + i);

            if(i == 20) {
                forkBlock = bestBlock;
            }
        }

        Chain chain = new Chain();
        chain.setEndBlockHeader(forkBlock.getHeader());
        chain.setStartBlockHeader(forkBlock.getHeader());
        chain.getBlockList().add(forkBlock);

        Block newBlock = newBlock(forkBlock);
        chain.setEndBlockHeader(newBlock.getHeader());
        chain.getBlockList().add(newBlock);

        ChainContainer otherChainContainer = new ChainContainer(chain);
        ChainContainer newForkChainContainer = chainContainer.getAfterTheForkChain(otherChainContainer);
        assertEquals(newForkChainContainer.getBestBlock().getHeader().getHeight(), 30L);

    }

    protected void addTx(Block block) {

        BlockHeader blockHeader = block.getHeader();
        List<Transaction> txs = block.getTxs();
                
        Transaction<Agent> agentTx = new RegisterAgentTransaction();
        Agent agent = new Agent();
        agent.setPackingAddress(new byte[22]);
        agent.setAgentAddress(new byte[21]);
        agent.setTime(System.currentTimeMillis());
        agent.setDeposit(Na.NA.multiply(20000));
        agent.setAgentName("test".getBytes());
        agent.setIntroduction("test agent".getBytes());
        agent.setCommissionRate(0.3d);
        agent.setBlockHeight(blockHeader.getHeight());

        agentTx.setTxData(agent);
        agentTx.setTime(agent.getTime());
        agentTx.setBlockHeight(blockHeader.getHeight());

        // add the agent tx into agent list
        txs.add(agentTx);

        // new a deposit
        Deposit deposit = new Deposit();
        deposit.setAddress(new byte[21]);
        deposit.setAgentHash(agentTx.getHash());
        deposit.setTime(System.currentTimeMillis());
        deposit.setDeposit(Na.NA.multiply(200000));
        deposit.setBlockHeight(blockHeader.getHeight());

        JoinConsensusTransaction depositTx = new JoinConsensusTransaction();
        depositTx.setTime(deposit.getTime());
        depositTx.setTxData(deposit);
        depositTx.setBlockHeight(blockHeader.getHeight());

        txs.add(depositTx);

    }

    protected Block newBlock(Block preBlock) {

        assertNotNull(preBlock);
        assertNotNull(preBlock.getHeader());

        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(preBlock.getHeader().getHeight() + 1);
        blockHeader.setPreHash(preBlock.getHeader().getHash());
        blockHeader.setTxCount(0);

        MeetingRound currentRound = chainContainer.getOrResetCurrentRound(false);
        MeetingMember member = currentRound.getMember(1);
        blockHeader.setPackingAddress(member.getPackingAddress());
        blockHeader.setTime(member.getPackEndTime() + ConsensusConstant.BLOCK_TIME_INTERVAL_MILLIS * currentRound.getMemberCount());

        // add a round data
        BlockRoundData roundData = new BlockRoundData(preBlock.getHeader().getExtend());
        roundData.setConsensusMemberCount(currentRound.getMemberCount());
        roundData.setPackingIndexOfRound(1);
        roundData.setRoundIndex(currentRound.getIndex() + 1);
        roundData.setRoundStartTime(currentRound.getEndTime());
        blockHeader.setExtend(roundData.serialize());

        // new a block of height 0
        Block block = new Block();
        block.setHeader(blockHeader);

        List<Transaction> txs = new ArrayList<>();
        block.setTxs(txs);

        return block;
    }
}
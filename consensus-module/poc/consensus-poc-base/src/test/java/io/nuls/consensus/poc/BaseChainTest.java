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

package io.nuls.consensus.poc;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.service.AccountService;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.entity.Agent;
import io.nuls.consensus.entity.Deposit;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.customer.ConsensusAccountServiceImpl;
import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.tx.JoinConsensusTransaction;
import io.nuls.consensus.tx.RegisterAgentTransaction;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.*;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Created by ln on 2018/5/7.
 */
public class BaseChainTest extends BaseTest {

    protected Chain chain;
    protected ChainContainer chainContainer;

    protected void initChain() {
        chain = new Chain();

        // new a block header
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(0);
        blockHeader.setPreHash(NulsDigestData.calcDigestData("00000000000".getBytes()));
        blockHeader.setTime(1L);
        blockHeader.setTxCount(0);

        // add a round data
        BlockRoundData roundData = new BlockRoundData();
        roundData.setConsensusMemberCount(1);
        roundData.setPackingIndexOfRound(1);
        roundData.setRoundIndex(1);
        roundData.setRoundStartTime(1L);
        blockHeader.setExtend(roundData.serialize());

        chain.setEndBlockHeader(blockHeader);

        // new a block of height 0
        Block block = new Block();
        block.setHeader(blockHeader);

        // add the block into chain
        chain.getBlockList().add(block);
        chain.setStartBlockHeader(blockHeader);
        chain.setEndBlockHeader(blockHeader);

        // init some agent
        List<Transaction<Agent>> agentList = new ArrayList<>();

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
        agentList.add(agentTx);

        // set the agent list into chain
        chain.setAgentList(agentList);

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

        List<Transaction<Deposit>> depositList = new ArrayList<>();

        depositList.add(depositTx);

        chain.setDepositList(depositList);

        chain.setYellowPunishList(new ArrayList<>());
        chain.setRedPunishList(new ArrayList<>());

        chainContainer = new ChainContainer(chain);
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
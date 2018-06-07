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

import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.protocol.tx.DepositTransaction;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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
        try {
            blockHeader.setExtend(roundData.serialize());
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }

        chain.setEndBlockHeader(blockHeader);

        // new a block of height 0
        Block block = new Block();
        block.setHeader(blockHeader);

        // add the block into chain
        chain.getBlockList().add(block);
        chain.setStartBlockHeader(blockHeader);
        chain.setEndBlockHeader(blockHeader);

        // init some agent
        List<Agent> agentList = new ArrayList<>();

        Transaction<Agent> agentTx = new CreateAgentTransaction();
        Agent agent = new Agent();
        agent.setPackingAddress(AddressTool.getAddress(ecKey.getPubKey()));
        agent.setAgentAddress(AddressTool.getAddress(new ECKey().getPubKey()));
        agent.setRewardAddress(AddressTool.getAddress(ecKey.getPubKey()));
        agent.setTime(System.currentTimeMillis());
        agent.setDeposit(Na.NA.multiply(20000));
        agent.setCommissionRate(0.3d);
        agent.setBlockHeight(blockHeader.getHeight());

        agentTx.setTxData(agent);
        agentTx.setTime(agent.getTime());
        agentTx.setBlockHeight(blockHeader.getHeight());

        NulsSignData signData = signDigest(agentTx.getHash().getDigestBytes(), ecKey);

        agentTx.setScriptSig(signData.getSignBytes());
        agentTx.getTxData().setTxHash(agentTx.getHash());

        // add the agent tx into agent list
        agentList.add(agentTx.getTxData());

        // set the agent list into chain
        chain.setAgentList(agentList);

        // new a deposit
        Deposit deposit = new Deposit();
        deposit.setAddress(AddressTool.getAddress(ecKey.getPubKey()));
        deposit.setAgentHash(agentTx.getHash());
        deposit.setTime(System.currentTimeMillis());
        deposit.setDeposit(Na.NA.multiply(200000));
        deposit.setBlockHeight(blockHeader.getHeight());

        DepositTransaction depositTx = new DepositTransaction();
        depositTx.setTime(deposit.getTime());
        depositTx.setTxData(deposit);
        depositTx.setBlockHeight(blockHeader.getHeight());

        List<Deposit> depositList = new ArrayList<>();

        depositList.add(depositTx.getTxData());

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
        blockHeader.setTxCount(1);

        MeetingRound round = chainContainer.initRound();

        BlockRoundData nextRoundData = new BlockRoundData();
        nextRoundData.setRoundIndex(round.getIndex() + 1);
        nextRoundData.setRoundStartTime(round.getEndTime());

        MeetingRound currentRound = chainContainer.getRoundManager().getNextRound(nextRoundData, false);

        MeetingMember member = currentRound.getMember(AddressTool.getAddress(ecKey.getPubKey()));
        blockHeader.setTime(member.getPackEndTime());

        // add a round data
        BlockRoundData roundData = new BlockRoundData(preBlock.getHeader().getExtend());
        roundData.setConsensusMemberCount(currentRound.getMemberCount());
        roundData.setPackingIndexOfRound(member.getPackingIndexOfRound());
        roundData.setRoundIndex(currentRound.getIndex());
        roundData.setRoundStartTime(currentRound.getStartTime());
        try {
            blockHeader.setExtend(roundData.serialize());
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }

        // new a block of height 0
        Block block = new Block();
        block.setHeader(blockHeader);

        List<Transaction> txs = new ArrayList<>();
        block.setTxs(txs);
        txs.add(new TestTransaction());

        List<NulsDigestData> txHashList = block.getTxHashList();

        blockHeader.setMerkleHash(NulsDigestData.calcMerkleDigestData(txHashList));

        NulsSignData signData = signDigest(blockHeader.getHash().getDigestBytes(), ecKey);

        P2PKHScriptSig sig = new P2PKHScriptSig();
        sig.setSignData(signData);
        sig.setPublicKey(ecKey.getPubKey());

        blockHeader.setScriptSig(sig);

        return block;
    }
}
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

import io.nuls.account.service.AccountService;
import io.nuls.consensus.poc.config.ConsensusConfig;
import io.nuls.consensus.poc.protocol.constant.PocConsensusProtocolConstant;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.locker.Lockers;
import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.core.tools.calc.DoubleUtils;
import io.nuls.core.tools.log.ConsensusLog;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.constant.ProtocolConstant;

import java.io.IOException;
import java.util.*;

/**
 * Created by ln on 2018/4/14.
 */
public class RoundManager {

    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);

    private List<MeetingRound> roundList = new ArrayList<>();

    private Chain chain;

    public RoundManager(Chain chain) {
        this.chain = chain;
    }

    public MeetingRound getRoundByIndex(long roundIndex) {
        MeetingRound round = null;
        for (int i = roundList.size() - 1; i >= 0; i--) {
            round = roundList.get(i);
            if (round.getIndex() == roundIndex) {
                break;
            }
        }
        return round;
    }

    public void addRound(MeetingRound meetingRound) {
        roundList.add(meetingRound);
    }

    public void checkIsNeedReset() {
        if (roundList == null || roundList.size() == 0) {
            initRound();
        } else {
            MeetingRound lastRound = roundList.get(roundList.size() - 1);
            Block bestBlcok = chain.getBestBlock();
            BlockRoundData blockRoundData = new BlockRoundData(bestBlcok.getHeader().getExtend());
            if (blockRoundData.getRoundIndex() < lastRound.getIndex()) {
                roundList.clear();
                initRound();
            }
        }
    }

    public boolean clearRound(int count) {
        if (roundList.size() > count) {
            roundList = roundList.subList(roundList.size() - count, roundList.size());
            MeetingRound round = roundList.get(0);
            round.setPreRound(null);
        }
        return true;
    }

    public MeetingRound getCurrentRound() {
        Lockers.ROUND_LOCK.lock();
        try {
            if (roundList == null || roundList.size() == 0) {
                return null;
            }
            MeetingRound round = roundList.get(roundList.size() - 1);
            if (round.getPreRound() == null && roundList.size() >= 2) {
                round.setPreRound(roundList.get(roundList.size() - 2));
            }
            return round;
        } finally {
            Lockers.ROUND_LOCK.unlock();
        }
    }

    public MeetingRound initRound() {
        MeetingRound currentRound = resetRound(false);

        if (currentRound.getPreRound() == null) {

            BlockRoundData roundData = null;
            List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
            for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
                BlockHeader blockHeader = blockHeaderList.get(i);
                roundData = new BlockRoundData(blockHeader.getExtend());
                if (roundData.getRoundIndex() < currentRound.getIndex()) {
                    break;
                }
            }
            MeetingRound preRound = getNextRound(roundData, false);
            currentRound.setPreRound(preRound);
        }

        return currentRound;
    }

    public MeetingRound resetRound(boolean isRealTime) {
        Lockers.ROUND_LOCK.lock();
        try {

            MeetingRound round = getCurrentRound();

            if (isRealTime) {
                if (round == null || round.getEndTime() < TimeService.currentTimeMillis()) {
                    MeetingRound nextRound = getNextRound(null, true);
                    nextRound.setPreRound(round);
                    roundList.add(nextRound);
                    round = nextRound;
                }
                return round;
            }

            BlockRoundData roundData = new BlockRoundData(chain.getEndBlockHeader().getExtend());

            if (round != null && roundData.getRoundIndex() == round.getIndex() && roundData.getPackingIndexOfRound() != roundData.getConsensusMemberCount()) {
                return round;
            }

            MeetingRound nextRound = getNextRound(null, false);
            if (round != null && nextRound.getIndex() <= round.getIndex()) {
                return nextRound;
            }
            nextRound.setPreRound(round);
            roundList.add(nextRound);
            return nextRound;
        } finally {
            Lockers.ROUND_LOCK.unlock();
        }
    }

    public MeetingRound getNextRound(BlockRoundData roundData, boolean isRealTime) {
        Lockers.ROUND_LOCK.lock();
        try {
            if (isRealTime && roundData == null) {
                return getNextRoudByRealTime();
            } else if (!isRealTime && roundData == null) {
                return getNextRoundByNotRealTime();
            } else {
                return getNextRoundByExpectedRound(roundData);
            }
        } finally {
            Lockers.ROUND_LOCK.unlock();
        }
    }

    private MeetingRound getNextRoudByRealTime() {

        BlockHeader bestBlockHeader = chain.getEndBlockHeader();

        BlockHeader startBlockHeader = bestBlockHeader;

        BlockRoundData bestRoundData = new BlockRoundData(bestBlockHeader.getExtend());

        if (startBlockHeader.getHeight() != 0L) {
            long roundIndex = bestRoundData.getRoundIndex();
            if (bestRoundData.getConsensusMemberCount() == bestRoundData.getPackingIndexOfRound() || TimeService.currentTimeMillis() >= bestRoundData.getRoundEndTime()) {
                roundIndex += 1;
            }
            startBlockHeader = getFirstBlockHeightOfPreRoundByRoundIndex(roundIndex);
        }

        long nowTime = TimeService.currentTimeMillis();
        long index = 0L;
        long startTime = 0L;

        if (nowTime < bestRoundData.getRoundEndTime()) {
            index = bestRoundData.getRoundIndex();
            startTime = bestRoundData.getRoundStartTime();
        } else {
            long diffTime = nowTime - bestRoundData.getRoundEndTime();
            int diffRoundCount = (int) (diffTime / (bestRoundData.getConsensusMemberCount() * ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L));
            index = bestRoundData.getRoundIndex() + diffRoundCount + 1;
            startTime = bestRoundData.getRoundEndTime() + diffRoundCount * bestRoundData.getConsensusMemberCount() * ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L;
        }
        return calculationRound(startBlockHeader, index, startTime);
    }

    private MeetingRound getNextRoundByNotRealTime() {
        BlockHeader bestBlockHeader = chain.getEndBlockHeader();
        BlockRoundData roundData = new BlockRoundData(bestBlockHeader.getExtend());
        roundData.setRoundStartTime(roundData.getRoundEndTime());
        roundData.setRoundIndex(roundData.getRoundIndex() + 1);
        return getNextRoundByExpectedRound(roundData);
    }

    private MeetingRound getNextRoundByExpectedRound(BlockRoundData roundData) {
        BlockHeader startBlockHeader = chain.getEndBlockHeader();

        long roundIndex = roundData.getRoundIndex();
        long roundStartTime = roundData.getRoundStartTime();
        if (startBlockHeader.getHeight() != 0L) {
//            if(roundData.getConsensusMemberCount() == roundData.getPackingIndexOfRound()) {
//                roundIndex += 1;
//                roundStartTime = roundData.getRoundEndTime();
//            }
            startBlockHeader = getFirstBlockHeightOfPreRoundByRoundIndex(roundIndex);
        }

        return calculationRound(startBlockHeader, roundIndex, roundStartTime);
    }

    private MeetingRound calculationRound(BlockHeader startBlockHeader, long index, long startTime) {

        MeetingRound round = new MeetingRound();

        round.setIndex(index);
        round.setStartTime(startTime);

        setMemberList(round, startBlockHeader);

        round.calcLocalPacker(accountService.getAccountList().getData());

        ConsensusLog.debug("calculation||index:{},startTime:{},startHeight:{},hash:{}\n" + round.toString() + "\n\n" + sb.toString(), index, startTime, startBlockHeader.getHeight(), startBlockHeader.getHash());
        return round;
    }

    StringBuilder sb = null;

    private void setMemberList(MeetingRound round, BlockHeader startBlockHeader) {

        sb = new StringBuilder();

        List<MeetingMember> memberList = new ArrayList<>();
        double totalWeight = 0;

        for (byte[] address : ConsensusConfig.getSeedNodeList()) {
            MeetingMember member = new MeetingMember();
            member.setAgentAddress(address);
            member.setPackingAddress(address);
            member.setRewardAddress(address);
            member.setCreditVal(1);
            member.setRoundStartTime(round.getStartTime());

            memberList.add(member);
        }

        // TODO removeSmallBlock the test code in future
        List<Transaction<Deposit>> depositTempList = new ArrayList<>();

        List<Transaction<Agent>> agentList = getAliveAgentList(startBlockHeader.getHeight());
        for (Transaction<Agent> tx : agentList) {
            Agent agent = tx.getTxData();

            MeetingMember member = new MeetingMember();
            member.setAgentTransaction(tx);
            member.setAgentHash(tx.getHash());
            member.setAgentAddress(agent.getAgentAddress());
            member.setRewardAddress(agent.getRewardAddress());
            member.setPackingAddress(agent.getPackingAddress());
            member.setOwnDeposit(agent.getDeposit());
            member.setCommissionRate(agent.getCommissionRate());
            member.setRoundStartTime(round.getStartTime());

            List<Transaction<Deposit>> cdlist = getDepositListByAgentId(tx.getHash(), startBlockHeader.getHeight());
            for (Transaction<Deposit> dtx : cdlist) {
                member.setTotalDeposit(member.getTotalDeposit().add(dtx.getTxData().getDeposit()));
                depositTempList.add(dtx);
            }

            member.setDepositList(cdlist);
            member.setCreditVal(calcCreditVal(member, startBlockHeader));
            agent.setCreditVal(member.getRealCreditVal());
            agent.setTotalDeposit(member.getTotalDeposit().getValue());

            boolean isItIn = member.getTotalDeposit().isGreaterOrEquals(PocConsensusProtocolConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT);
            if (isItIn) {
                totalWeight = DoubleUtils.sum(totalWeight, DoubleUtils.mul(agent.getDeposit().getValue(), member.getCalcCreditVal()));
                totalWeight = DoubleUtils.sum(totalWeight, DoubleUtils.mul(member.getTotalDeposit().getValue(), member.getCalcCreditVal()));

                memberList.add(member);
            }
        }

        Collections.sort(memberList);

        for (int i = 0; i < memberList.size(); i++) {
            MeetingMember member = memberList.get(i);
            member.setRoundIndex(round.getIndex());
            member.setPackingIndexOfRound(i + 1);
        }

        round.init(memberList);

        Collections.sort(depositTempList, new Comparator<Transaction<Deposit>>() {
            @Override
            public int compare(Transaction<Deposit> o1, Transaction<Deposit> o2) {
                try {
                    return o1.getHash().getDigestHex().compareTo(o2.getHash().getDigestHex());
                } catch (IOException e) {
                    Log.error(e);
                    throw new NulsRuntimeException(e);
                }
            }
        });

        for (Transaction<Deposit> cd : depositTempList) {
            Deposit deposit = cd.getTxData();
            sb.append("------------------------ agent hash : " + deposit.getAgentHash());
            sb.append("dep address : " + deposit.getAddress());
            sb.append(" , amount : " + deposit.getDeposit());
            sb.append(" , hash : " + deposit.getTxHash());
            sb.append(" , height : " + deposit.getBlockHeight());
            sb.append(" , del height : " + deposit.getDelHeight());
            sb.append("\n");
        }
    }

    private List<Transaction<Deposit>> getDepositListByAgentId(NulsDigestData agentHash, long startBlockHeight) {

        List<Transaction<Deposit>> depositList = chain.getDepositList();
        List<Transaction<Deposit>> resultList = new ArrayList<>();

        for (int i = depositList.size() - 1; i >= 0; i--) {
            Transaction<Deposit> tx = depositList.get(i);
            Deposit deposit = tx.getTxData();
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (!deposit.getAgentHash().equals(agentHash)) {
                continue;
            }
            resultList.add(tx);
        }

        return resultList;
    }

    private List<Transaction<Agent>> getAliveAgentList(long startBlockHeight) {
        List<Transaction<Agent>> resultList = new ArrayList<>();
        for (int i = chain.getAgentList().size() - 1; i >= 0; i--) {
            Transaction<Agent> tx = chain.getAgentList().get(i);
            Agent agent = tx.getTxData();
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            resultList.add(tx);
        }
        return resultList;
    }

    private double calcCreditVal(MeetingMember member, BlockHeader blockHeader) {

        BlockRoundData roundData = new BlockRoundData(blockHeader.getExtend());

        long roundStart = roundData.getRoundIndex() - PocConsensusProtocolConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        long blockCount = getBlockCountByAddress(member.getPackingAddress(), roundStart, roundData.getRoundIndex() - 1);
        long sumRoundVal = getPunishCountByAddress(member.getAgentAddress(), roundStart, roundData.getRoundIndex() - 1, PunishType.YELLOW.getCode());
        double ability = DoubleUtils.div(blockCount, PocConsensusProtocolConstant.RANGE_OF_CAPACITY_COEFFICIENT);

        double penalty = DoubleUtils.div(DoubleUtils.mul(PocConsensusProtocolConstant.CREDIT_MAGIC_NUM, sumRoundVal),
                DoubleUtils.mul(PocConsensusProtocolConstant.RANGE_OF_CAPACITY_COEFFICIENT, PocConsensusProtocolConstant.RANGE_OF_CAPACITY_COEFFICIENT));

//        BlockLog.debug(")))))))))))))creditVal:" + DoubleUtils.sub(ability, penalty) + ",member:" + member.getAgentAddress());
//        BlockLog.debug(")))))))))))))blockCount:" + blockCount + ", start:" + roundStart + ",end:" + calcRoundIndex + ", yellowCount:" + sumRoundVal);

        return DoubleUtils.round(DoubleUtils.sub(ability, penalty), 4);
    }

    private long getPunishCountByAddress(byte[] address, long roundStart, long roundEnd, int code) {
        long count = 0;
        List<PunishLogPo> punishList = chain.getYellowPunishList();

        if (code == PunishType.RED.getCode()) {
            punishList = chain.getRedPunishList();
        }

        for (int i = punishList.size() - 1; i >= 0; i--) {
            PunishLogPo punish = punishList.get(i);

            if (punish.getRoundIndex() > roundEnd) {
                continue;
            }
            if (punish.getRoundIndex() < roundStart) {
                break;
            }
            if (Arrays.equals(punish.getAddress(), address)) {
                count++;
            }
        }
        return count;
    }

    private long getBlockCountByAddress(byte[] packingAddress, long roundStart, long roundEnd) {
        long count = 0;
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();

        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            BlockRoundData roundData = new BlockRoundData(blockHeader.getExtend());

            if (roundData.getRoundIndex() > roundEnd) {
                continue;
            }
            if (roundData.getRoundIndex() < roundStart) {
                break;
            }
            if (Arrays.equals(blockHeader.getPackingAddress(), packingAddress)) {
                count++;
            }
        }
        return count;
    }

    private BlockHeader getFirstBlockHeightOfPreRoundByRoundIndex(long roundIndex) {
        BlockHeader firstBlockHeader = null;
        long startRoundIndex = 0L;
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            long currentRoundIndex = new BlockRoundData(blockHeader.getExtend()).getRoundIndex();
            if (roundIndex > currentRoundIndex) {
                if (startRoundIndex == 0L) {
                    startRoundIndex = currentRoundIndex;
                }
                if (currentRoundIndex < startRoundIndex) {
                    firstBlockHeader = blockHeaderList.get(i + 1);
                    break;
                }
            }
        }
        if (firstBlockHeader == null) {
            firstBlockHeader = chain.getStartBlockHeader();
            Log.warn("the first block of pre round not found");
        }
        return firstBlockHeader;
    }

    public Chain getChain() {
        return chain;
    }

    public List<MeetingRound> getRoundList() {
        return roundList;
    }
}

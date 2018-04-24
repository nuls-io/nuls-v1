/*
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

package io.nuls.consensus.poc.manager;

import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.poc.locker.Lockers;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.protocol.constant.ConsensusStatusEnum;
import io.nuls.consensus.poc.protocol.constant.PocConsensusConstant;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.context.ConsensusContext;
import io.nuls.consensus.poc.protocol.model.Agent;
import io.nuls.consensus.poc.protocol.model.Deposit;
import io.nuls.consensus.poc.protocol.model.MeetingMember;
import io.nuls.consensus.poc.protocol.model.MeetingRound;
import io.nuls.consensus.poc.protocol.model.block.BlockRoundData;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.ConsensusLog;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.PunishLogPo;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.BlockHeader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        nextRound.setPreRound(round);
        roundList.add(nextRound);
        return nextRound;
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

        if (startBlockHeader.getHeight() != 0l) {
            long roundIndex = bestRoundData.getRoundIndex();
            if (bestRoundData.getConsensusMemberCount() == bestRoundData.getPackingIndexOfRound() || TimeService.currentTimeMillis() >= bestRoundData.getRoundEndTime()) {
                roundIndex += 1;
            }
            startBlockHeader = getFirstBlockHeightOfPreRoundByRoundIndex(roundIndex);
        }

        long nowTime = TimeService.currentTimeMillis();
        long index = 0l;
        long startTime = 0l;

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
        return getNextRoundByExpectedRound(roundData);
    }

    private MeetingRound getNextRoundByExpectedRound(BlockRoundData roundData) {
        BlockHeader startBlockHeader = chain.getEndBlockHeader();

        long roundIndex = roundData.getRoundIndex();
        long roundStartTime = roundData.getRoundStartTime();
        if (startBlockHeader.getHeight() != 0l) {
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

        round.calcLocalPacker(accountService.getAccountList());

        ConsensusLog.debug("calculation||index:{},startTime:{},startHeight:{},hash:{}\n" + round.toString(), index, startTime, startBlockHeader.getHeight(), startBlockHeader.getHash());
        return round;
    }

    private void setMemberList(MeetingRound round, BlockHeader startBlockHeader) {

        List<MeetingMember> memberList = new ArrayList<>();
        double totalWeight = 0;
        for (String address : ConsensusContext.getSeedNodeList()) {
            MeetingMember member = new MeetingMember();
            member.setAgentAddress(address);
            member.setPackingAddress(address);
            member.setCreditVal(1);
            member.setRoundStartTime(round.getStartTime());

            memberList.add(member);
        }
        List<Consensus<Agent>> agentList = getAliveAgentList(startBlockHeader.getHeight());
        for (Consensus<Agent> ca : agentList) {
            MeetingMember member = new MeetingMember();
            member.setAgentConsensus(ca);
            member.setAgentHash(ca.getHexHash());
            member.setAgentAddress(ca.getAddress());
            member.setPackingAddress(ca.getExtend().getPackingAddress());
            member.setOwnDeposit(ca.getExtend().getDeposit());
            member.setCommissionRate(ca.getExtend().getCommissionRate());
            member.setRoundStartTime(round.getStartTime());

            List<Consensus<Deposit>> cdlist = getDepositListByAgentId(ca.getHexHash(), startBlockHeader.getHeight());
            for (Consensus<Deposit> cd : cdlist) {
                member.setTotalDeposit(member.getTotalDeposit().add(cd.getExtend().getDeposit()));
            }
            member.setDepositList(cdlist);
            member.setCreditVal(calcCreditVal(member, startBlockHeader));
            ca.getExtend().setCreditVal(member.getRealCreditVal());
            ca.getExtend().setTotalDeposit(member.getTotalDeposit().getValue());
            boolean isItIn = member.getTotalDeposit().isGreaterOrEquals(PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT);
            if (isItIn) {
                ca.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
                totalWeight = DoubleUtils.sum(totalWeight, DoubleUtils.mul(ca.getExtend().getDeposit().getValue(), member.getCalcCreditVal()));
                totalWeight = DoubleUtils.sum(totalWeight, DoubleUtils.mul(member.getTotalDeposit().getValue(), member.getCalcCreditVal()));
                memberList.add(member);
            } else {
                ca.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
            }
        }

        Collections.sort(memberList);

        for (int i = 0; i < memberList.size(); i++) {
            MeetingMember member = memberList.get(i);
            member.setRoundIndex(round.getIndex());
            member.setPackingIndexOfRound(i + 1);
        }

        round.setMemberCount(memberList.size());
        round.setMemberList(memberList);
        round.setEndTime(round.getStartTime() + memberList.size() * ProtocolConstant.BLOCK_TIME_INTERVAL_MILLIS);
        round.setTotalWeight(totalWeight);
    }

    private List<Consensus<Deposit>> getDepositListByAgentId(String agentId, long startBlockHeight) {

        List<Consensus<Deposit>> depositList = chain.getDepositList();
        List<Consensus<Deposit>> resultList = new ArrayList<>();

        for (int i = depositList.size() - 1; i >= 0; i--) {
            Consensus<Deposit> cd = depositList.get(i);
            if (cd.getDelHeight() != 0 && cd.getDelHeight() <= startBlockHeight) {
                continue;
            }
            Deposit deposit = cd.getExtend();
            if (deposit.getBlockHeight() >= startBlockHeight || deposit.getBlockHeight() < 0) {
                continue;
            }
            if (!deposit.getAgentHash().equals(agentId)) {
                continue;
            }
            resultList.add(cd);
        }

        return resultList;
    }

    private List<Consensus<Agent>> getAliveAgentList(long startBlockHeight) {
        List<Consensus<Agent>> resultList = new ArrayList<>();
        for (int i = chain.getAgentList().size() - 1; i >= 0; i--) {
            Consensus<Agent> ca = chain.getAgentList().get(i);
            if (ca.getDelHeight() != 0 && ca.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (ca.getExtend().getBlockHeight() >= startBlockHeight || ca.getExtend().getBlockHeight() < 0) {
                continue;
            }
            resultList.add(ca);
        }
        return resultList;
    }

    private double calcCreditVal(MeetingMember member, BlockHeader blockHeader) {

        BlockRoundData roundData = new BlockRoundData(blockHeader.getExtend());

        long roundStart = roundData.getRoundIndex() - PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT + 1;
        if (roundStart < 0) {
            roundStart = 0;
        }
        long blockCount = getBlockCountByAddress(member.getPackingAddress(), roundStart, roundData.getRoundIndex(),blockHeader.getHeight());
        long sumRoundVal = getPunishCountByAddress(member.getAgentAddress(), roundStart, roundData.getRoundIndex(), blockHeader.getHeight(), PunishType.YELLOW.getCode());
        double ability = DoubleUtils.div(blockCount, PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);

        double penalty = DoubleUtils.div(DoubleUtils.mul(PocConsensusConstant.CREDIT_MAGIC_NUM, sumRoundVal),
                DoubleUtils.mul(PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT, PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT));

//        BlockLog.debug(")))))))))))))creditVal:" + DoubleUtils.sub(ability, penalty) + ",member:" + member.getAgentAddress());
//        BlockLog.debug(")))))))))))))blockCount:" + blockCount + ", start:" + roundStart + ",end:" + calcRoundIndex + ", yellowCount:" + sumRoundVal);

        return ability - penalty;
    }

    private long getPunishCountByAddress(String address, long roundStart, long roundEnd, long maxHeight, int code) {
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
            if (punish.getHeight() > maxHeight) {
                continue;
            }
            if (punish.getAddress().equals(address)) {
                count++;
            }
        }
        return count;
    }

    private long getBlockCountByAddress(String packingAddress, long roundStart, long roundEnd,long maxHeight) {
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
            if (blockHeader.getHeight() > maxHeight) {
                continue;
            }
            if (Address.fromHashs(blockHeader.getPackingAddress()).getBase58().equals(packingAddress)) {
                count++;
            }
        }
        return count;
    }

    private BlockHeader getFirstBlockHeightOfPreRoundByRoundIndex(long roundIndex) {
        BlockHeader firstBlockHeader = null;
        long startRoundIndex = 0l;
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            long currentRoundIndex = new BlockRoundData(blockHeader.getExtend()).getRoundIndex();
            if (roundIndex > currentRoundIndex) {
                if (startRoundIndex == 0l) {
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
}

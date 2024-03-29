/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.consensus.poc.locker.Lockers;
import io.nuls.consensus.poc.model.*;
import io.nuls.consensus.poc.protocol.constant.PocConsensusProtocolConstant;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.core.tools.calc.DoubleUtils;
import io.nuls.core.tools.log.ConsensusLog;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.protocol.constant.ProtocolConstant;

import java.util.*;

/**
 * @author ln
 */
public class RoundManager {

    private AccountService accountService;

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
            Lockers.ROUND_LOCK.lock();
            try {
                MeetingRound lastRound = roundList.get(roundList.size() - 1);
                Block bestBlcok = chain.getBestBlock();
                BlockExtendsData blockRoundData = new BlockExtendsData(bestBlcok.getHeader().getExtend());
                if (blockRoundData.getRoundIndex() < lastRound.getIndex()) {
                    roundList.clear();
                    initRound();
                }
            } finally {
                Lockers.ROUND_LOCK.unlock();
            }
        }
    }

    public boolean clearRound(int count) {
        Lockers.ROUND_LOCK.lock();
        boolean doit = false;
        try {
            while (roundList.size() > count) {
                doit = true;
                roundList.remove(0);
            }
            if (doit) {
                MeetingRound round = roundList.get(0);
                round.setPreRound(null);
            }
            return true;
        } finally {
            Lockers.ROUND_LOCK.unlock();
        }
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

            BlockExtendsData extendsData = null;
            List<BlockHeader> blockHeaderList = chain.getAllBlockHeaderList();
            for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
                BlockHeader blockHeader = blockHeaderList.get(i);
                extendsData = new BlockExtendsData(blockHeader.getExtend());
                if (extendsData.getRoundIndex() < currentRound.getIndex()) {
                    break;
                }
            }
            MeetingRound preRound = getNextRound(extendsData, false);
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

            BlockExtendsData extendsData = new BlockExtendsData(chain.getEndBlockHeader().getExtend());
            if (round != null && extendsData.getRoundIndex() == round.getIndex() && extendsData.getPackingIndexOfRound() != extendsData.getConsensusMemberCount()) {
                return round;
            }

            MeetingRound nextRound = getNextRound(extendsData, false);
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

    public MeetingRound getNextRound(BlockExtendsData roundData, boolean isRealTime) {
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

        BlockExtendsData bestRoundData = new BlockExtendsData(bestBlockHeader.getExtend());

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
        BlockExtendsData extendsData = new BlockExtendsData(bestBlockHeader.getExtend());
        extendsData.setRoundStartTime(extendsData.getRoundEndTime());
        extendsData.setRoundIndex(extendsData.getRoundIndex() + 1);
        return getNextRoundByExpectedRound(extendsData);
    }

    private MeetingRound getNextRoundByExpectedRound(BlockExtendsData roundData) {
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

        round.calcLocalPacker(getAccountService().getAccountList().getData());

        ConsensusLog.debug("\ncalculation||index:{},startTime:{},startHeight:{},hash:{}\n" + round.toString() + "\n\n", index, startTime, startBlockHeader.getHeight(), startBlockHeader.getHash());
        return round;
    }


    private void setMemberList(MeetingRound round, BlockHeader startBlockHeader) {

        List<MeetingMember> memberList = new ArrayList<>();
        double totalWeight = 0;

        for (byte[] address : ConsensusConfig.getSeedNodeList()) {
            MeetingMember member = new MeetingMember();
            member.setAgentAddress(address);
            member.setPackingAddress(address);
            member.setRewardAddress(address);
            member.setCreditVal(0);
            member.setRoundStartTime(round.getStartTime());
            memberList.add(member);
        }

        if (NulsContext.isNetFinished(NulsConfig.MODULES_CONFIG.getCfgValue(PocConsensusProtocolConstant.CFG_CONSENSUS_SECTION, PocConsensusProtocolConstant.STOP_DELAY, Integer.MAX_VALUE))) {
            round.init(memberList);
            return;
        }

        List<Deposit> depositTempList = new ArrayList<>();

        BlockExtendsData roundData = new BlockExtendsData(startBlockHeader.getExtend());

        long roundStart = roundData.getRoundIndex() - PocConsensusProtocolConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        long roundEnd = roundData.getRoundIndex() - 1;

        Map<String, Integer> blockCountMap = getBlockCountMap(roundStart, roundEnd);
        Map<String, Integer> punishCountMap = getPunishCountMap(roundStart, roundEnd, PunishType.YELLOW.getCode());
        Map<NulsDigestData, List<Deposit>> agentDepositMap = getDepositListMap(startBlockHeader.getHeight());

        List<Agent> agentList = getAliveAgentList(startBlockHeader.getHeight());
        for (Agent agent : agentList) {


            List<Deposit> cdlist = agentDepositMap.get(agent.getTxHash());

            if (null == cdlist) {
                continue;
            }

            MeetingMember member = new MeetingMember();
            member.setAgent(agent);
            member.setAgentHash(agent.getTxHash());
            member.setAgentAddress(agent.getAgentAddress());
            member.setRewardAddress(agent.getRewardAddress());
            member.setPackingAddress(agent.getPackingAddress());
            member.setOwnDeposit(agent.getDeposit());
            member.setCommissionRate(agent.getCommissionRate());
            member.setRoundStartTime(round.getStartTime());
            member.setPackingAddressStr(agent.getPackingAddressStr());
            member.setAgentAddressStr(agent.getAgentAddressStr());


            for (Deposit dtx : cdlist) {
                member.setTotalDeposit(member.getTotalDeposit().add(dtx.getDeposit()));
                depositTempList.add(dtx);
            }
            member.setDepositList(cdlist);
            agent.setTotalDeposit(member.getTotalDeposit().getValue());
            boolean isItIn = member.getTotalDeposit().isGreaterOrEquals(PocConsensusProtocolConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT);
            if (isItIn) {
                member.setCreditVal(calcCreditVal(blockCountMap.get(member.getPackingAddressStr()), punishCountMap.get(member.getAgentAddressStr())));
                agent.setCreditVal(member.getRealCreditVal());
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

        Collections.sort(depositTempList, new Comparator<Deposit>() {
            @Override
            public int compare(Deposit o1, Deposit o2) {
                return o1.getTxHash().getDigestHex().compareTo(o2.getTxHash().getDigestHex());
            }
        });
    }

    private Map<NulsDigestData, List<Deposit>> getDepositListMap(long startBlockHeight) {

        List<Deposit> depositList = chain.getDepositList();
        Map<NulsDigestData, List<Deposit>> map = new HashMap<>();

        for (int i = depositList.size() - 1; i >= 0; i--) {
            Deposit deposit = depositList.get(i);
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            List<Deposit> list = map.get(deposit.getAgentHash());
            if (null == list) {
                list = new ArrayList<>();
                map.put(deposit.getAgentHash(), list);
            }
            list.add(deposit);
        }

        return map;
    }

    private List<Agent> getAliveAgentList(long startBlockHeight) {
        List<Agent> resultList = new ArrayList<>();
        for (int i = chain.getAgentList().size() - 1; i >= 0; i--) {
            Agent agent = chain.getAgentList().get(i);
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            resultList.add(agent);
        }
        return resultList;
    }

    private double calcCreditVal(Integer blockCount, Integer punishCount) {
        if (null == blockCount) {
            blockCount = 0;
        }

        if (null == punishCount) {
            punishCount = 0;
        }

        //每一轮的惩罚都有可能包含上一轮次的惩罚记录，即计算从a到a+99轮的惩罚记录时，a轮的惩罚中可能是惩罚某个地址在a-1轮未出块，导致100轮最多可能有101个惩罚记录，在这里处理下
        //Each round of punishment is likely to contain a rounds punishment record, calculated from a to a + 99 rounds of punishment record,
        // a round of punishment is likely to be punished in an address in a - 1 round not out of the blocks,
        // lead to round up to 100 May be 101 punishment record, treatment here
        if (punishCount > 100) {
            punishCount = 100;
        }


        double ability = DoubleUtils.div(blockCount, PocConsensusProtocolConstant.RANGE_OF_CAPACITY_COEFFICIENT);

        double penalty = DoubleUtils.div(DoubleUtils.mul(PocConsensusProtocolConstant.CREDIT_MAGIC_NUM, punishCount),
                DoubleUtils.mul(PocConsensusProtocolConstant.RANGE_OF_CAPACITY_COEFFICIENT, PocConsensusProtocolConstant.RANGE_OF_CAPACITY_COEFFICIENT));

        double value = DoubleUtils.round(DoubleUtils.sub(ability, penalty), 4);
        return value;
    }

    private Map<String, Integer> getPunishCountMap(long roundStart, long roundEnd, int code) {
        List<PunishLogPo> punishList = new ArrayList<>(chain.getYellowPunishList());

        if (code == PunishType.RED.getCode()) {
            punishList = chain.getRedPunishList();
        }

        Map<String, Integer> map = new HashMap<>();

        for (int i = punishList.size() - 1; i >= 0; i--) {
            PunishLogPo punish = punishList.get(i);

            if (punish.getRoundIndex() > roundEnd) {
                continue;
            }
            if (punish.getRoundIndex() < roundStart) {
                break;
            }
            Integer count = map.get(punish.getAddressStr());
            if (null == count) {
                count = 0;
            }
            map.put(punish.getAddressStr(), count + 1);
        }

        return map;
    }

    private Map<String, Integer> getBlockCountMap(long roundStart, long roundEnd) {
        List<BlockHeader> blockHeaderList = chain.getAllBlockHeaderList();
        Map<String, Integer> map = new HashMap<>();
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());

            if (roundData.getRoundIndex() > roundEnd) {
                continue;
            }
            if (roundData.getRoundIndex() < roundStart) {
                break;
            }

            Integer count = map.get(blockHeader.getPackingAddressStr());
            if (null == count) {
                count = 0;
            }
            map.put(blockHeader.getPackingAddressStr(), count + 1);
        }
        return map;
    }

    private BlockHeader getFirstBlockHeightOfPreRoundByRoundIndex(long roundIndex) {
        BlockHeader firstBlockHeader = null;
        long startRoundIndex = 0L;
        List<BlockHeader> blockHeaderList = chain.getAllBlockHeaderList();
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            long currentRoundIndex = new BlockExtendsData(blockHeader.getExtend()).getRoundIndex();
            if (roundIndex > currentRoundIndex) {
                if (startRoundIndex == 0L) {
                    startRoundIndex = currentRoundIndex;
                }
                if (currentRoundIndex < startRoundIndex) {
                    firstBlockHeader = blockHeaderList.get(i + 1);
                    BlockExtendsData roundData = new BlockExtendsData(firstBlockHeader.getExtend());
                    if (roundData.getPackingIndexOfRound() > 1) {
                        firstBlockHeader = blockHeader;
                    }
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

    public AccountService getAccountService() {
        if (accountService == null) {
            accountService = NulsContext.getServiceBean(AccountService.class);
        }
        return accountService;
    }
}

package io.nuls.consensus.manager;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.constant.PunishType;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.service.impl.PocBlockService;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.core.utils.log.ConsensusLog;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.PunishLogDataService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Niels Wang
 * @date: 2018/4/2
 */
public class RoundManager {

    private static final RoundManager INSTANCE = new RoundManager();

    private static final int CACHE_COUNT = 5;
    private static final Map<Long, PocMeetingRound> ROUND_MAP = new ConcurrentHashMap<>(CACHE_COUNT);

    private ConsensusManager csManager = ConsensusManager.getInstance();
    private PocBlockService pocBlockService = PocBlockService.getInstance();
    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();
    private PunishLogDataService punishLogDataService = NulsContext.getServiceBean(PunishLogDataService.class);


    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);


    private BlockService consensusBlockService;
    private PocMeetingRound currentRound;

    private Lock lock = new ReentrantLock();


    public void init() {
        lock.lock();
        try {
            realInit();
        } finally {
            lock.unlock();
        }
    }

    private void realInit() {
        consensusCacheManager.clear();
        consensusCacheManager.init();

        //load five(CACHE_COUNT) round from db on the start time ;
        Block bestBlock = getBestBlock();
        BlockRoundData roundData = new BlockRoundData(bestBlock.getHeader().getExtend());
        boolean updateCacheStatus = true;
        for (long i = roundData.getRoundIndex(); i >= 1 && i >= roundData.getRoundIndex() - CACHE_COUNT; i--) {
            Block firstBlock = getBlockService().getRoundFirstBlock(i - 1);
            BlockRoundData preRoundData = new BlockRoundData(firstBlock.getHeader().getExtend());
            PocMeetingRound round = calcRound(firstBlock.getHeader().getHeight(), i, preRoundData.getRoundEndTime(), updateCacheStatus);
            ROUND_MAP.put(round.getIndex(), round);
            Log.debug("load the round data index:{}", round.getIndex());
            updateCacheStatus = false;
        }
    }

    private RoundManager() {
    }

    public static RoundManager getInstance() {
        return INSTANCE;
    }

    public PocMeetingRound resetCurrentMeetingRound() {
        lock.lock();
        try {
            Block currentBlock = getBestBlock();
            return getRoundByBlock(currentBlock);
        } finally {
            lock.unlock();
        }
    }

    private PocMeetingRound getRoundByBlock(Block currentBlock) {
        BlockRoundData currentRoundData = new BlockRoundData(currentBlock.getHeader().getExtend());
        boolean needCalcRound = false;
        do {
            if (null == currentRound) {
                needCalcRound = true;
                break;
            }
            if (currentRound.getEndTime() < TimeService.currentTimeMillis()) {
                needCalcRound = true;
                break;
            }
            boolean thisIsLastBlockOfRound = currentRoundData.getPackingIndexOfRound() == currentRoundData.getConsensusMemberCount();
            if (currentRound.getIndex() == currentRoundData.getRoundIndex() && !thisIsLastBlockOfRound) {
                needCalcRound = false;
                break;
            }
            if (currentRound.getIndex() == (currentRoundData.getRoundIndex() + 1) && thisIsLastBlockOfRound) {
                needCalcRound = false;
                break;
            }
            needCalcRound = true;
        } while (false);
        PocMeetingRound resultRound = null;
        if (needCalcRound) {
            resultRound = calcNextRound(currentBlock, currentRoundData, true);
            BlockLog.debug("calc NextRound:" + resultRound.getIndex() + ", preIndex:" + currentRoundData.getRoundIndex());
        } else {
            resultRound = this.currentRound;
        }
        if (!this.getBestBlock().getHeader().getHash().equals(currentBlock.getHeader().getHash())) {
            return this.getRoundByBlock(this.getBestBlock());
        }
        if (resultRound.getPreRound() == null) {
            resultRound.setPreRound(ROUND_MAP.get(currentRoundData.getRoundIndex()));
        }

        List<Account> accountList = accountService.getAccountList();
        resultRound.calcLocalPacker(accountList);
        this.currentRound = resultRound;
        ROUND_MAP.put(resultRound.getIndex(), resultRound);
        return resultRound;

    }

    private PocMeetingRound calcNextRound(Block calcBlock, BlockRoundData blockRoundData, boolean updateCacheStatus) {
//        boolean calcBlockRoundFinished = blockRoundData.getRoundEndTime() < TimeService.currentTimeMillis() || blockRoundData.getConsensusMemberCount() == blockRoundData.getPackingIndexOfRound();
//        if(calcBlockRoundFinished){
//        }else{
//        }
        Block lastRoundFirstBlock = getBlockService().getRoundFirstBlock(blockRoundData.getRoundIndex());
        PocMeetingRound round = calcRound(lastRoundFirstBlock.getHeader().getHeight(), blockRoundData.getRoundIndex() + 1, blockRoundData.getRoundEndTime(), updateCacheStatus);

        ConsensusLog.info("计算：根据的高度：" + calcBlock.getHeader().getHeight() + ",该块轮次：" + blockRoundData.getRoundIndex() + ",start:" + blockRoundData.getRoundStartTime() + ", members:" + blockRoundData.getConsensusMemberCount() + ",roundCount:" + round.getMemberCount());

        boolean needCalcOrder = false;
        while (round.getEndTime() <= TimeService.currentTimeMillis()) {
            long time = TimeService.currentTimeMillis() - round.getStartTime();
            long roundTime = PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L * round.getMemberCount();
            long index = time / roundTime;
            long startTime = round.getStartTime() + index * roundTime;
            round.setStartTime(startTime);
            round.setIndex(round.getIndex() + index);
            needCalcOrder = true;
        }
        if (needCalcOrder) {
            List<PocMeetingMember> memberList = round.getMemberList();
            for (PocMeetingMember member : memberList) {
                member.setRoundIndex(round.getIndex());
                member.setRoundStartTime(round.getStartTime());
            }
            Collections.sort(memberList);
            round.setMemberList(memberList);
        }


        StringBuilder str = new StringBuilder();
        for (PocMeetingMember member : round.getMemberList()) {
            str.append(member.getPackingAddress());
            str.append(" ,creditVal:" + member.getRealCreditVal());
            str.append(" ,order:" + member.getPackingIndexOfRound());
            str.append(",packTime:" + new Date(member.getPackEndTime()));
            str.append("\n");
        }
        if (null == round.getPreRound()) {
            BlockLog.debug("calc new round:index:" + round.getIndex() + " , start:" + new Date(round.getStartTime())
                    + ", netTime:(" + new Date(TimeService.currentTimeMillis()).toString() + ") , members:\n :" + str);
        } else {
            BlockLog.debug("calc new round:index:" + round.getIndex() + " ,preIndex:" + round.getPreRound().getIndex() + " , start:" + new Date(round.getStartTime())
                    + ", netTime:(" + new Date(TimeService.currentTimeMillis()).toString() + ") , members:\n :" + str);
        }
        return round;
    }

    private PocMeetingRound calcRound(long startCalcHeight, long roundIndex, long startTIme, boolean updateCacheStatus) {
        ConsensusLog.info("++++++++calcRound:height:" + startCalcHeight + " ,index:" + roundIndex, " ,startTime");
        PocMeetingRound round = new PocMeetingRound();
        round.setIndex(roundIndex);
        round.setStartTime(startTIme);
        List<PocMeetingMember> memberList = getMemberList(startCalcHeight, round, updateCacheStatus);
        Collections.sort(memberList);
        round.setMemberList(memberList);
        return round;
    }

    private List<PocMeetingMember> getMemberList(long startCalcHeight, PocMeetingRound round, boolean updateCacheStatus) {
        List<PocMeetingMember> memberList = new ArrayList<>();
        double totalWeight = 0;
        for (String address : csManager.getSeedNodeList()) {
            PocMeetingMember member = new PocMeetingMember();
            member.setAgentAddress(address);
            member.setPackingAddress(address);
            member.setCreditVal(0);
            member.setRoundStartTime(round.getStartTime());
            memberList.add(member);
        }
        List<Consensus<Agent>> agentList = consensusCacheManager.getAliveAgentList(startCalcHeight);
        BlockLog.debug("agent list cache:size:" + consensusCacheManager.agentKeySet().size());
        BlockLog.debug("deposit list cache:size:" + consensusCacheManager.depositKeySet().size());
        BlockLog.debug("get alive agent list form cache:size:" + agentList.size());
        for (Consensus<Agent> ca : agentList) {
            PocMeetingMember member = new PocMeetingMember();
            member.setAgentConsensus(ca);
            member.setAgentHash(ca.getHexHash());
            member.setAgentAddress(ca.getAddress());
            member.setPackingAddress(ca.getExtend().getPackingAddress());
            member.setOwnDeposit(ca.getExtend().getDeposit());
            member.setCommissionRate(ca.getExtend().getCommissionRate());
            member.setRoundStartTime(round.getStartTime());
            List<Consensus<Deposit>> cdlist = consensusCacheManager.getDepositListByAgentId(ca.getHexHash(), startCalcHeight);
            BlockLog.debug("get alive depositlist by agentId:" + ca.getHexHash() + " , calcHeight:" + startCalcHeight + " ,resultSize:" + cdlist.size());
            for (Consensus<Deposit> cd : cdlist) {
                member.setTotalDeposit(member.getTotalDeposit().add(cd.getExtend().getDeposit()));
            }
            member.setDepositList(cdlist);
            member.setCreditVal(calcCreditVal(member, startCalcHeight - PocConsensusConstant.CONFIRM_BLOCK_COUNT, round.getIndex() - 2));
            ca.getExtend().setCreditVal(member.getRealCreditVal());
            ca.getExtend().setTotalDeposit(member.getTotalDeposit().getValue());
            boolean isItIn = member.getTotalDeposit().isGreaterOrEquals(PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT);
            if (isItIn) {
                ca.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
                totalWeight = DoubleUtils.sum(totalWeight, DoubleUtils.mul(ca.getExtend().getDeposit().getValue(), member.getCalcCreditVal()));
                totalWeight = DoubleUtils.sum(totalWeight, DoubleUtils.mul(member.getTotalDeposit().getValue(), member.getCalcCreditVal()));
                memberList.add(member);
                if (updateCacheStatus) {
                    ca.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
                    this.consensusCacheManager.updateAgent(ca);
                    this.consensusCacheManager.updateDepositStatusByAgentId(ca.getHexHash(), startCalcHeight, ConsensusStatusEnum.IN);
                }
            } else {
                ca.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
                if (updateCacheStatus) {
                    this.consensusCacheManager.updateAgent(ca);
                    this.consensusCacheManager.updateDepositStatusByAgentId(ca.getHexHash(), startCalcHeight, ConsensusStatusEnum.WAITING);
                }
            }
        }
        round.setTotalWeight(totalWeight);
        return memberList;
    }

    private double calcCreditVal(PocMeetingMember member, long startHeight, long calcRoundIndex) {
        if (calcRoundIndex == 0) {
            return 0;
        }
        long roundStart = calcRoundIndex - PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        long blockCount = pocBlockService.getBlockCount(member.getPackingAddress(), roundStart, calcRoundIndex, startHeight);
        long sumRoundVal = punishLogDataService.getCountByRounds(member.getAgentAddress(), roundStart, calcRoundIndex, startHeight, PunishType.YELLOW.getCode());
        double ability = DoubleUtils.div(blockCount, PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);

        double penalty = DoubleUtils.div(DoubleUtils.mul(PocConsensusConstant.CREDIT_MAGIC_NUM, sumRoundVal),
                DoubleUtils.mul(PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT, PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT));
        BlockLog.debug(")))))))))))))creditVal:" + DoubleUtils.sub(ability, penalty) + ",member:" + member.getAgentAddress());
        BlockLog.debug(")))))))))))))blockCount:" + blockCount + ", start:" + roundStart + ",end:" + calcRoundIndex + ", yellowCount:" + sumRoundVal);
        return DoubleUtils.sub(ability, penalty);
    }

    private BlockService getBlockService() {
        if (null == this.consensusBlockService) {
            consensusBlockService = NulsContext.getServiceBean(BlockService.class);
        }
        return consensusBlockService;
    }

    public PocMeetingRound getCurrentRound() {
        List<Account> accountList = accountService.getAccountList();
        currentRound.calcLocalPacker(accountList);
        return currentRound;
    }


    private Block getBestBlock() {
        Block block = NulsContext.getInstance().getBestBlock();
        Block highestBlock = BlockManager.getInstance().getHighestBlock();
        if (null != highestBlock && highestBlock.getHeader().getHeight() > block.getHeader().getHeight()) {
            return highestBlock;
        }
        return block;
    }

    public PocMeetingRound getRound(BlockHeader currentBlockHeader, long preRoundIndex, long roundIndex, boolean needPreRound) {
        lock.lock();
        try {
            return getTheRound(currentBlockHeader, preRoundIndex, roundIndex, needPreRound);
        } finally {
            lock.unlock();
        }
    }

    private PocMeetingRound getTheRound(BlockHeader currentBlockHeader, long preRoundIndex, long roundIndex, boolean needPreRound) {

        PocMeetingRound round = ROUND_MAP.get(roundIndex);
        Block preRoundFirstBlock = null;
        BlockRoundData preRoundData = null;
        if (BlockManager.getInstance().getStoredHeight() < (currentBlockHeader.getHeight() - PocConsensusConstant.CONFIRM_BLOCK_COUNT - 1)) {
            Log.debug("Round can't be calculated for the time being");
            return null;
        }
        if (null == round) {
            Block bestBlock = getBestBlock();
            BlockRoundData nowRoundData = new BlockRoundData(bestBlock.getHeader().getExtend());
            if (nowRoundData.getRoundIndex() >= preRoundIndex) {
                preRoundFirstBlock = getBlockService().getRoundFirstBlock(preRoundIndex);
                if (null == preRoundFirstBlock) {
                    return null;
                }
                preRoundData = new BlockRoundData(preRoundFirstBlock.getHeader().getExtend());
                round = calcRound(preRoundFirstBlock.getHeader().getHeight(), roundIndex, preRoundData.getRoundEndTime(), false);
                if (roundIndex > (preRoundData.getRoundIndex() + 1)) {
                    long roundTime = PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L * round.getMemberCount();
                    long startTime = round.getStartTime() + (roundIndex - (preRoundData.getRoundIndex() + 1)) * roundTime;
                    round.setStartTime(startTime);
                    List<PocMeetingMember> memberList = round.getMemberList();
                    for (PocMeetingMember member : memberList) {
                        member.setRoundStartTime(round.getStartTime());
                    }
                    Collections.sort(memberList);
                    round.setMemberList(memberList);
                }
                ROUND_MAP.put(round.getIndex(), round);
            } else {
                return null;
            }
        }
        if (needPreRound && round.getPreRound() == null) {
            if (null == preRoundFirstBlock) {
                Block firstBlock = getBlockService().getRoundFirstBlock(preRoundIndex);
                if (null == firstBlock) {
                    return null;
                }
                preRoundFirstBlock = firstBlock;
                preRoundData = new BlockRoundData(preRoundFirstBlock.getHeader().getExtend());
            }
            if (preRoundFirstBlock.getHeader().getHeight() == 0) {
                round.setPreRound(calcRound(0, 1, preRoundData.getRoundStartTime(), false));
                return round;
            }
            Block preblock = getBlockService().getBlock(preRoundFirstBlock.getHeader().getPreHash().getDigestHex());
            if (null == preblock) {
                return null;
            }
            BlockRoundData preBlockRoundData = new BlockRoundData(preblock.getHeader().getExtend());
            PocMeetingRound preRound = getRound(currentBlockHeader, preBlockRoundData.getRoundIndex(), preRoundIndex, false);
            if (preRound.getIndex() != preRoundIndex && preRoundIndex > 1) {
                System.out.println();
            }
            round.setPreRound(preRound);
        }
        StringBuilder str = new StringBuilder();
        for (PocMeetingMember member : round.getMemberList()) {
            str.append(member.getPackingAddress());
            str.append(" ,order:" + member.getPackingIndexOfRound());
            str.append(" ,creditVal:" + member.getRealCreditVal());
            str.append(",packEndTime:" + new Date(member.getPackEndTime()));
            str.append("\n");
        }
        if (null == round.getPreRound()) {
            BlockLog.debug("calc new round:index:" + round.getIndex() + " , start:" + new Date(round.getStartTime())
                    + ", netTime:(" + new Date(TimeService.currentTimeMillis()).toString() + ") , members:\n :" + str);
        } else {
            BlockLog.debug("calc new round:index:" + round.getIndex() + " ,preIndex:" + round.getPreRound().getIndex() + " , start:" + new Date(round.getStartTime())
                    + ", netTime:(" + new Date(TimeService.currentTimeMillis()).toString() + ") , members:\n :" + str);
        }
        return round;
    }

    public void clearAndInit() {
        lock.lock();
        try {
            ROUND_MAP.clear();
            this.realInit();
        } finally {
            lock.unlock();
        }
    }
}

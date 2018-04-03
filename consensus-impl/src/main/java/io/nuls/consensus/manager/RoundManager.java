package io.nuls.consensus.manager;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.ConsensusReward;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.service.impl.PocBlockService;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.params.OperationType;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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


    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);


    private BlockService consensusBlockService;

    public void init() {
        //load five(CACHE_COUNT) round from db on the start time ;
        Block bestBlock = NulsContext.getInstance().getBestBlock();
        BlockRoundData roundData = new BlockRoundData(bestBlock.getHeader().getExtend());
        for (long i = roundData.getRoundIndex(); i >= 1 && i >= roundData.getRoundIndex() - CACHE_COUNT; i--) {
            Block firstBlock = getBlockService().getRoundFirstBlock(bestBlock, i - 1);
            BlockRoundData thisRoundData = new BlockRoundData(firstBlock.getHeader().getExtend());
            PocMeetingRound round = calcRound(firstBlock.getHeader().getHeight(), i, thisRoundData.getRoundStartTime());
            ROUND_MAP.put(round.getIndex(), round);
            Log.debug("load the round data index:{}", round.getIndex());
        }
    }

    private RoundManager() {
    }

    public static RoundManager getPackingRoundManager() {
        return INSTANCE;
    }


    public PocMeetingRound getCurrentRound() {
        Block currentBlock = NulsContext.getInstance().getBestBlock();
        BlockRoundData currentRoundData = new BlockRoundData(currentBlock.getHeader().getExtend());
        PocMeetingRound round = ROUND_MAP.get(currentRoundData.getRoundIndex());
        if (null == round) {
            round = resetCurrentMeetingRound();
        }
        return round;
    }


    public synchronized PocMeetingRound resetCurrentMeetingRound() {
        Block currentBlock = NulsContext.getInstance().getBestBlock();
        BlockRoundData currentRoundData = new BlockRoundData(currentBlock.getHeader().getExtend());
        PocMeetingRound currentRound = ROUND_MAP.get(currentRoundData.getRoundIndex());
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
            if (null != ROUND_MAP.get(currentRoundData.getRoundIndex() + 1)) {
                resultRound = ROUND_MAP.get(currentRoundData.getRoundIndex() + 1);
            } else {
                resultRound = calcNextRound(currentBlock, currentRoundData);
            }
        }

        if (resultRound.getPreRound() == null) {
            resultRound.setPreRound(ROUND_MAP.get(currentRoundData.getRoundIndex() + 1));
        }

        List<Account> accountList = accountService.getAccountList();
        resultRound.calcLocalPacker(accountList);
        return resultRound;
    }


    private PocMeetingRound calcNextRound(Block calcBlock, BlockRoundData blockRoundData) {
//        boolean calcBlockRoundFinished = blockRoundData.getRoundEndTime() < TimeService.currentTimeMillis() || blockRoundData.getConsensusMemberCount() == blockRoundData.getPackingIndexOfRound();
//        if(calcBlockRoundFinished){
//        }else{
//        }
        Block lastRoundFirstBlock = getBlockService().getRoundFirstBlock(calcBlock, blockRoundData.getRoundIndex());
        PocMeetingRound round = calcRound(lastRoundFirstBlock.getHeader().getHeight(), blockRoundData.getRoundIndex() + 1, blockRoundData.getRoundEndTime());
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
        return round;
    }

    private PocMeetingRound calcRound(long startCalcHeight, long roundIndex, long startTIme) {
        PocMeetingRound round = new PocMeetingRound();
        round.setIndex(roundIndex);
        round.setStartTime(startTIme);
        List<PocMeetingMember> memberList = getMemberList(startCalcHeight, round);
        Collections.sort(memberList);
        round.setMemberList(memberList);
        return round;
    }

    private List<PocMeetingMember> getMemberList(long startCalcHeight, PocMeetingRound round) {
        List<PocMeetingMember> memberList = new ArrayList<>();
        Na totalDeposit = Na.ZERO;
        for (String address : csManager.getSeedNodeList()) {
            PocMeetingMember member = new PocMeetingMember();
            member.setAgentAddress(address);
            member.setPackingAddress(address);
            member.setCreditVal(1);
            member.setRoundStartTime(round.getStartTime());
            memberList.add(member);
        }
        List<Consensus<Agent>> agentList = getAgentList(startCalcHeight);
        Map<String, List<DepositPo>> depositMap = new HashMap<>();
        if (agentList.size() > 0) {
            List<DepositPo> depositPoList = depositDataService.getAllList(startCalcHeight);
            for (DepositPo depositPo : depositPoList) {
                List<DepositPo> subList = depositMap.get(depositPo.getAgentHash());
                if (null == subList) {
                    subList = new ArrayList<>();
                    depositMap.put(depositPo.getAgentHash(), subList);
                }
                subList.add(depositPo);
            }
        }
        Set<String> agentSet = consensusCacheManager.agentKeySet();
        Set<String> depositKeySet = consensusCacheManager.depositKeySet();

        for (Consensus<Agent> ca : agentList) {
            PocMeetingMember member = new PocMeetingMember();
            member.setAgentConsensus(ca);
            member.setAgentHash(ca.getHexHash());
            member.setAgentAddress(ca.getAddress());
            member.setPackingAddress(ca.getExtend().getPackingAddress());
            member.setOwnDeposit(ca.getExtend().getDeposit());
            member.setCommissionRate(ca.getExtend().getCommissionRate());
            totalDeposit = totalDeposit.add(ca.getExtend().getDeposit());
            List<DepositPo> depositPoList = depositMap.get(ca.getHexHash());
            if (depositPoList != null) {
                List<Consensus<Deposit>> cdlist = new ArrayList<>();
                for (DepositPo depositPo : depositPoList) {
                    Consensus<Deposit> cd = ConsensusTool.fromPojo(depositPo);
                    member.setTotalDeposit(member.getTotalDeposit().add(cd.getExtend().getDeposit()));
                    cdlist.add(cd);
                }
                member.setDepositList(cdlist);
            }
            totalDeposit = totalDeposit.add(member.getTotalDeposit());
            member.setCreditVal(calcCreditVal(member, round.getIndex() - 2));
            if (member.getTotalDeposit().isGreaterThan(PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT)) {
                ca.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
                memberList.add(member);
            } else {
                ca.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
            }
            consensusCacheManager.cacheAgent(ca);
            agentSet.remove(ca.getHexHash());
            for (Consensus<Deposit> cd : member.getDepositList()) {
                cd.getExtend().setStatus(ca.getExtend().getStatus());
                consensusCacheManager.cacheDeposit(cd);
                depositKeySet.remove(cd.getHexHash());
            }
        }
        for (String key : agentSet) {
            consensusCacheManager.removeAgent(key);
        }
        for (String key : depositKeySet) {
            consensusCacheManager.removeDeposit(key);
        }
        round.setTotalDeposit(totalDeposit);
        return memberList;
    }

    /**
     * get agent list from db
     */
    private List<Consensus<Agent>> getAgentList(long calcHeight) {
        List<AgentPo> poList = agentDataService.getAllList(calcHeight);
        if (null == poList || poList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Consensus<Agent>> agentList = new ArrayList<>();
        for (AgentPo po : poList) {
            Consensus<Agent> agent = ConsensusTool.fromPojo(po);
            agentList.add(agent);
        }
        return agentList;
    }

    private double calcCreditVal(PocMeetingMember member, long calcRoundIndex) {
        if (calcRoundIndex == 0) {
            return 0;
        }
        long roundStart = calcRoundIndex - PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        long blockCount = pocBlockService.getBlockCount(member.getAgentAddress(), roundStart, calcRoundIndex);
        long sumRoundVal = pocBlockService.getSumOfRoundIndexOfYellowPunish(member.getAgentAddress(), roundStart, calcRoundIndex);
        double ability = blockCount / PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;

        double penalty = (PocConsensusConstant.CREDIT_MAGIC_NUM * sumRoundVal) / (PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT * PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);
        return ability - penalty;
    }

    private BlockService getBlockService() {
        if (null == this.consensusBlockService) {
            consensusBlockService = NulsContext.getServiceBean(BlockService.class);
        }
        return consensusBlockService;
    }


    public CoinBaseTransaction createNewCoinBaseTx(PocMeetingMember member, List<Transaction> txList, PocMeetingRound localRound) {
        CoinTransferData data = new CoinTransferData(OperationType.COIN_BASE, Na.ZERO);
        List<ConsensusReward> rewardList = calcReward(txList, member, localRound);
        Na total = Na.ZERO;
        for (ConsensusReward reward : rewardList) {
            Coin coin = new Coin();
            coin.setNa(reward.getReward());
            data.addTo(reward.getAddress(), coin);
            total = total.add(reward.getReward());
        }
        data.setTotalNa(total);
        CoinBaseTransaction tx;
        try {
            tx = new CoinBaseTransaction(data, null);
            tx.setTime(member.getPackEndTime());
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        tx.setFee(Na.ZERO);
        tx.setHash(NulsDigestData.calcDigestData(tx));
        return tx;
    }

    private List<ConsensusReward> calcReward(List<Transaction> txList, PocMeetingMember self, PocMeetingRound localRound) {
        List<ConsensusReward> rewardList = new ArrayList<>();
        if (self.getOwnDeposit().getValue() == Na.ZERO.getValue()) {
            long totalFee = 0;
            for (Transaction tx : txList) {
                totalFee += tx.getFee().getValue();
            }
            if (totalFee == 0L) {
                return rewardList;
            }
            double caReward = totalFee;
            ConsensusReward agentReword = new ConsensusReward();
            agentReword.setAddress(self.getAgentAddress());
            agentReword.setReward(Na.valueOf((long) caReward));
            rewardList.add(agentReword);
            return rewardList;
        }
        long totalFee = 0;
        for (Transaction tx : txList) {
            totalFee += tx.getFee().getValue();
        }
        double totalAll = DoubleUtils.mul(localRound.getMemberCount(), PocConsensusConstant.BLOCK_REWARD.getValue());

        double blockReword = totalFee + DoubleUtils.mul(totalAll, DoubleUtils.div(self.getOwnDeposit().getValue() + self.getTotalDeposit().getValue(), localRound.getTotalDeposit().getValue()));

        ConsensusReward agentReword = new ConsensusReward();
        agentReword.setAddress(self.getAgentAddress());
        double caReward = DoubleUtils.mul(blockReword, DoubleUtils.mul(DoubleUtils.div((localRound.getTotalDeposit().getValue() - self.getOwnDeposit().getValue()), localRound.getTotalDeposit().getValue()
        ), DoubleUtils.round(self.getCommissionRate() / 100, 2)));
        agentReword.setReward(Na.valueOf((long) caReward));
        Map<String, ConsensusReward> rewardMap = new HashMap<>();
        rewardMap.put(self.getAgentAddress(), agentReword);
        double delegateCommissionRate = DoubleUtils.div((100 - self.getCommissionRate()), 100, 2);
        for (Consensus<Deposit> cd : self.getDepositList()) {
            double reward =
                    DoubleUtils.mul(DoubleUtils.mul(blockReword, delegateCommissionRate),
                            DoubleUtils.div(cd.getExtend().getDeposit().getValue(),
                                    localRound.getTotalDeposit().getValue()));

            ConsensusReward delegateReword = rewardMap.get(cd.getAddress());
            if (null == delegateReword) {
                delegateReword = new ConsensusReward();
                delegateReword.setReward(Na.ZERO);
            }
            delegateReword.setAddress(cd.getAddress());
            delegateReword.setReward(delegateReword.getReward().add(Na.valueOf((long) reward)));
            rewardMap.put(cd.getAddress(), delegateReword);
        }

        rewardList.addAll(rewardMap.values());
        return rewardList;
    }

}

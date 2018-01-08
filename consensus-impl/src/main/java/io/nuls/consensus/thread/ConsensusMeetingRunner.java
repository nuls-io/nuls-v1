package io.nuls.consensus.thread;

import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.YellowPunishData;
import io.nuls.consensus.entity.block.BlockData;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.ConsensusGroup;
import io.nuls.consensus.entity.meeting.ConsensusReward;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.consensus.utils.TxComparator;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;
import io.nuls.ledger.service.intf.LedgerService;

import java.io.IOException;
import java.util.*;

/**
 * @author Niels
 * @date 2017/12/15
 */
public class ConsensusMeetingRunner implements Runnable {
    public static final String THREAD_NAME = "Consensus-Meeting";
    private static final ConsensusMeetingRunner INSTANCE = new ConsensusMeetingRunner();
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();
    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();
    private BlockService blockService = BlockServiceImpl.getInstance();
    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();
    private NetworkEventBroadcaster networkEventBroadcaster = NulsContext.getInstance().getService(NetworkEventBroadcaster.class);
    private boolean running = false;
    private NulsContext context = NulsContext.getInstance();
    private ConsensusManager consensusManager = ConsensusManager.getInstance();

    private static Map<Long, RedPunishData> punishMap = new HashMap<>();

    private ConsensusMeetingRunner() {
    }

    public static ConsensusMeetingRunner getInstance() {
        return INSTANCE;
    }

    public static void putPunishData(RedPunishData redPunishData) {
        punishMap.put(redPunishData.getHeight(), redPunishData);
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        this.running = true;
        while (running) {
            try {
                nextRound();
            } catch (Exception e) {
                Log.error(e);
                Log.info("meeting exception&restart meeting");
                startMeeting();
            }
        }
    }

    private void nextRound() {
        PocMeetingRound currentRound = calcRound();
        consensusManager.setCurrentRound(currentRound);
        while (TimeService.currentTimeMillis() < (currentRound.getStartTime())) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }

        List<Consensus<Agent>> list = calcConsensusAgentList();
        currentRound.setMemberCount(list.size());
        List<PocMeetingMember> memberList = new ArrayList<>();
        ConsensusGroup cg = new ConsensusGroup();
        Na totalDeposit = Na.ZERO;
        Na agentTotalDeposit = Na.ZERO;

        for (Consensus<Agent> ca : list) {
            PocMeetingMember mm = new PocMeetingMember();
            mm.setRoundIndex(currentRound.getIndex());
            mm.setAddress(ca.getAddress());
            mm.setPackerAddress(ca.getExtend().getDelegateAddress());
            mm.setRoundStartTime(currentRound.getStartTime());
            memberList.add(mm);
            if (ca.getAddress().equals(consensusManager.getLocalAccountAddress())) {
                cg.setAgentConsensus(ca);
                agentTotalDeposit = agentTotalDeposit.add(ca.getExtend().getDeposit());
            }
            totalDeposit = totalDeposit.add(ca.getExtend().getDeposit());
        }
        Collections.sort(memberList);
        currentRound.setMemberList(memberList);
        List<Consensus<Delegate>> delegateList = consensusCacheManager.getCachedDelegateList();
        List<Consensus<Delegate>> myDelegateList = new ArrayList<>();
        for (Consensus<Delegate> cd : delegateList) {
            totalDeposit = totalDeposit.add(cd.getExtend().getDeposit());
            if (cd.getExtend().getDelegateAddress().equals(consensusManager.getLocalAccountAddress())) {
                myDelegateList.add(cd);
                agentTotalDeposit = agentTotalDeposit.add(cd.getExtend().getDeposit());
            }
        }
        currentRound.setTotalDeposit(totalDeposit);
        currentRound.setAgentTotalDeposit(agentTotalDeposit);
        currentRound.setEndTime(currentRound.getStartTime() + currentRound.getMemberCount() * PocConsensusConstant.BLOCK_TIME_INTERVAL * 1000L);
        cg.setDelegateList(myDelegateList);
        currentRound.setConsensusGroup(cg);
        startMeeting();
    }

    private void startMeeting() {
        PocMeetingMember self = consensusManager.getCurrentRound().getMember(consensusManager.getLocalAccountAddress());
        if (null == self) {
            this.nextRound();
            return;
        }
        self.setCreditVal(calcCreditVal());
        long timeUnit = 100L;
        while (TimeService.currentTimeMillis() >= (self.getPackTime() - timeUnit)) {
            try {
                Thread.sleep(timeUnit);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
        packing(self);
    }

    private double calcCreditVal() {
        long roundStart = consensusManager.getCurrentRound().getIndex() - 1 - PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        int blockCount = blockService.getBlockCount(consensusManager.getLocalAccountAddress(), roundStart, consensusManager.getCurrentRound().getIndex());
        int sumRoundVal = 1;
        //todo blockService.getSumOfYellowPunishRound(consensusManager.getLocalAccountAddress());

        double ability = blockCount / PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        double penalty = (PocConsensusConstant.CREDIT_MAGIC_NUM * sumRoundVal) / (consensusManager.getCurrentRound().getIndex() * consensusManager.getCurrentRound().getIndex());

        return ability - penalty;
    }

    private void packing(PocMeetingMember self) {
        Block bestBlock = blockService.getLocalBestBlock();
        List<Transaction> txList = txCacheManager.getTxList();
        txList.sort(new TxComparator());
        addConsensusTx(bestBlock, txList, self);
        BlockData bd = new BlockData();
        bd.setHeight(bestBlock.getHeader().getHeight() + 1);
        bd.setTime(self.getPackTime());
        bd.setPreHash(bestBlock.getHeader().getHash());
        BlockRoundData roundData = new BlockRoundData();
        roundData.setRoundIndex(consensusManager.getCurrentRound().getIndex());
        roundData.setConsensusMemberCount(consensusManager.getCurrentRound().getMemberCount());
        roundData.setPackingIndexOfRound(self.getIndexOfRound());
        roundData.setRoundStartTime(consensusManager.getCurrentRound().getStartTime());
        bd.setRoundData(roundData);
        bd.setTxList(txList);
        Block newBlock = ConsensusTool.createBlock(bd);
        newBlock.verify();
        blockCacheManager.cacheBlock(newBlock);
        BlockHeaderEvent event = new BlockHeaderEvent();
        event.setEventBody(newBlock.getHeader());
        networkEventBroadcaster.broadcastAndCache(event);
    }

    /**
     * CoinBase transaction & Punish transaction
     *
     * @param bestBlock local highest block
     * @param txList    all tx of block
     * @param self      agent meeting data
     */
    private void addConsensusTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) {
        punishTx(bestBlock, txList, self);
        coinBaseTx(txList);
    }

    private void coinBaseTx(List<Transaction> txList) {
        CoinTransferData data = new CoinTransferData();
        data.setFee(Na.ZERO);
        List<ConsensusReward> rewardList = calcReward(txList);
        Na total = Na.ZERO;
        for (ConsensusReward reward : rewardList) {
            Coin coin = new Coin();
            coin.setNa(reward.getReward());
            data.addTo(reward.getAddress(), coin);
            total = total.add(reward.getReward());
        }
        data.setTotalNa(total);
        CoinBaseTransaction tx = new CoinBaseTransaction(data, null);
        tx.setFee(Na.ZERO);
        tx.setHash(NulsDigestData.calcDigestData(tx));
        tx.setSign(accountService.signData(tx.getHash()));
        ValidateResult validateResult = null;
        validateResult = ledgerService.verifyTx(tx);
        //todo cache
        if (null == validateResult || validateResult.isFailed()) {
            throw new NulsRuntimeException(ErrorCode.CONSENSUS_EXCEPTION);
        }
        txList.add(0, tx);
    }

    private List<ConsensusReward> calcReward(List<Transaction> txList) {
        List<ConsensusReward> rewardList = new ArrayList<>();
        ConsensusGroup cg = this.consensusManager.getCurrentRound().getConsensusGroup();
        long totalFee = 0;
        for (Transaction tx : txList) {
            totalFee += tx.getFee().getValue();
        }
        double total = totalFee + PocConsensusConstant.ANNUAL_INFLATION.getValue() *
                (PocConsensusConstant.BLOCK_TIME_INTERVAL * this.consensusManager.getCurrentRound().getMemberCount()) / PocConsensusConstant.BLOCK_COUNT_OF_YEAR
                * (this.consensusManager.getCurrentRound().getAgentTotalDeposit().getValue() / this.consensusManager.getCurrentRound().getTotalDeposit().getValue());
        Consensus<Agent> ca = cg.getAgentConsensus();
        double caReward = total * ((ca.getExtend().getDeposit().getValue() / this.consensusManager.getCurrentRound().getTotalDeposit().getValue())
                + (((this.consensusManager.getCurrentRound().getTotalDeposit().getValue() - ca.getExtend().getDeposit().getValue()) / this.consensusManager.getCurrentRound().getTotalDeposit().getValue()
        ) * ca.getExtend().getCommissionRate()));
        ConsensusReward agentReword = new ConsensusReward();
        agentReword.setAddress(ca.getAddress());
        agentReword.setReward(Na.valueOf((long) caReward));
        rewardList.add(agentReword);
        for (Consensus<Delegate> cd : cg.getDelegateList()) {
            double reward = total *
                    (cd.getExtend().getDeposit().getValue() / this.consensusManager.getCurrentRound().getTotalDeposit().getValue()) *
                    (1 - ca.getExtend().getCommissionRate());
            ConsensusReward delegateReword = new ConsensusReward();
            delegateReword.setAddress(cd.getAddress());
            delegateReword.setReward(Na.valueOf((long) reward));
            rewardList.add(delegateReword);
        }
        return rewardList;
    }

    private void punishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) {
        boolean b = redPunishTx(bestBlock, txList);
        if (!b) {
            yellowPunishTx(bestBlock, txList, self);
        }
    }

    private boolean redPunishTx(Block bestBlock, List<Transaction> txList) {
        boolean punish = false;
        for (long height : punishMap.keySet()) {
            RedPunishData data = punishMap.get(height);
            punishMap.remove(height);
            if (data.getHeight() < (bestBlock.getHeader().getHeight() + 1)) {
                continue;
            }
            RedPunishTransaction tx = new RedPunishTransaction();
            tx.setTxData(data);
            tx.setTime(TimeService.currentTimeMillis());
            tx.setFee(Na.ZERO);
            tx.setHash(NulsDigestData.calcDigestData(tx));
            tx.setSign(accountService.signData(tx.getHash()));
            txList.add(tx);
            punish = true;
        }
        return punish;
    }

    private void yellowPunishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) {
        BlockRoundData lastBlockRoundData = new BlockRoundData();
        try {
            lastBlockRoundData.parse(bestBlock.getHeader().getExtend());
        } catch (NulsException e) {
            Log.error(e);
        }
        boolean punish = self.getIndexOfRound() == 1 && lastBlockRoundData.getPackingIndexOfRound() != lastBlockRoundData.getConsensusMemberCount();
        punish = punish || (self.getIndexOfRound() > 1 && self.getIndexOfRound() != (lastBlockRoundData.getPackingIndexOfRound() + 1));
        if (!punish) {
            return;
        }
        PocMeetingMember previous = this.consensusManager.getCurrentRound().getMember(self.getIndexOfRound() - 1);
        if (null == previous) {
            return;
        }
        YellowPunishTransaction punishTx = new YellowPunishTransaction();
        YellowPunishData data = new YellowPunishData();
        data.setAddress(previous.getAddress());
        data.setHeight(bestBlock.getHeader().getHeight() + 1);
        punishTx.setTxData(data);
        punishTx.setTime(TimeService.currentTimeMillis());
        punishTx.setFee(Na.ZERO);
        punishTx.setHash(NulsDigestData.calcDigestData(punishTx));
        punishTx.setSign(accountService.signData(punishTx.getHash()));
        txList.add(punishTx);
    }

    private PocMeetingRound calcRound() {
        Block bestBlock = blockService.getLocalBestBlock();
        PocMeetingRound round = new PocMeetingRound(this.consensusManager.getCurrentRound());
        do {
            if (bestBlock.getHeader().getHeight() == 1) {
                round.setStartTime(this.context.getGenesisBlock().getHeader().getTime() + 10000L);
                break;
            }
            BlockRoundData lastRoundData;
            try {
                lastRoundData = new BlockRoundData(bestBlock.getHeader().getExtend());
            } catch (NulsException e) {
                Log.error(e);
                throw new NulsRuntimeException(e);
            }
            round.setStartTime(lastRoundData.getRoundEndTime());
        } while (false);
        return round;
    }

    private List<Consensus<Agent>> getDefaultSeedList() throws IOException {
        List<Consensus<Agent>> seedList = new ArrayList<>();
        Properties prop = ConfigLoader.loadProperties(PocConsensusConstant.DEFAULT_CONSENSUS_LIST_FILE);
        if (null == prop || prop.isEmpty()) {
            return seedList;
        }
        for (Object key : prop.keySet()) {
            String address = prop.getProperty((String) key);
            Consensus<Agent> member = new Consensus<>();
            member.setAddress(address);
            Agent agent = new Agent();
            agent.setDelegateAddress(address);
            agent.setStartTime(0);
            agent.setIntroduction("seed");
            agent.setCommissionRate(0);
            agent.setStatus(ConsensusStatusEnum.IN.getCode());
            agent.setSeed(true);
            seedList.add(member);
        }
        return seedList;
    }

    private List<Consensus<Agent>> calcConsensusAgentList() {
        List<Consensus<Agent>> list = new ArrayList<>();
        list.addAll(consensusCacheManager.getCachedAgentList(ConsensusStatusEnum.IN));
        if (list.size() >= PocConsensusConstant.MIN_CONSENSUS_AGENT_COUNT) {
            return list;
        }
        try {
            List<Consensus<Agent>> seedList = getDefaultSeedList();
            list.addAll(seedList);
        } catch (IOException e) {
            Log.error(e);
        }
        return list;
    }

    public boolean isRunning() {
        return running;
    }

}

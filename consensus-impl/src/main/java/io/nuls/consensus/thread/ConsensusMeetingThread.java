package io.nuls.consensus.thread;

import io.nuls.account.service.intf.AccountService;
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
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.cache.ConsensusCacheService;
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
import io.nuls.event.bus.bus.service.intf.EventBroadcaster;
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
public class ConsensusMeetingThread implements Runnable {
    public static final String THREAD_NAME = "Consensus-Meeting";
    private static final ConsensusMeetingThread INSTANCE = new ConsensusMeetingThread();
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);
    private String localAccountAddress;
    private ConsensusCacheService consensusCacheService = ConsensusCacheService.getInstance();
    private BlockCacheService blockCacheService = BlockCacheService.getInstance();
    private BlockService blockService = BlockServiceImpl.getInstance();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);
    private boolean running = false;
    private NulsContext context = NulsContext.getInstance();
    private PocMeetingRound currentRound;

    private static Map<Long, RedPunishData> punishMap = new HashMap<>();

    private ConsensusMeetingThread() {
    }

    public static ConsensusMeetingThread getInstance() {
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
        this.localAccountAddress = accountService.getDefaultAccount();
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
        currentRound = calcRound();
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
        for (Consensus<Agent> ca : list) {
            PocMeetingMember mm = new PocMeetingMember();
            mm.setRoundIndex(currentRound.getIndex());
            mm.setAddress(ca.getAddress());
            mm.setPackerAddress(ca.getExtend().getDelegateAddress());
            mm.setRoundStartTime(currentRound.getStartTime());
            memberList.add(mm);
            if (ca.getAddress().equals(localAccountAddress)) {
                cg.setAgentConsensus(ca);
            }
        }
        Collections.sort(memberList);
        currentRound.setMemberList(memberList);
        currentRound.setEndTime(currentRound.getStartTime() + currentRound.getMemberCount() * PocConsensusConstant.BLOCK_TIME_INTERVAL * 1000L);
        cg.setDelegateList(consensusCacheService.getCachedDelegateList(localAccountAddress));
        currentRound.setConsensusGroup(cg);
        startMeeting();
    }

    private void startMeeting() {
        PocMeetingMember self = currentRound.getMember(localAccountAddress);
        if (null == self) {
            this.nextRound();
            return;
        }
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

    private void packing(PocMeetingMember self) {
        Block bestBlock = blockService.getLocalBestBlock();
        List<Transaction> txList = ledgerService.getTxListFromCache();
        txList.sort(new TxComparator());
        addConsensusTx(bestBlock, txList, self);
        BlockData bd = new BlockData();
        bd.setHeight(bestBlock.getHeader().getHeight() + 1);
        bd.setTime(self.getPackTime());
        bd.setPreHash(bestBlock.getHeader().getHash());
        BlockRoundData roundData = new BlockRoundData();
        roundData.setRoundIndex(currentRound.getIndex());
        roundData.setConsensusMemberCount(currentRound.getMemberCount());
        roundData.setPackingIndexOfRound(self.getIndexOfRound());
        roundData.setRoundStartTime(currentRound.getStartTime());
        bd.setRoundData(roundData);
        bd.setTxList(txList);
        Block newBlock = ConsensusTool.createBlock(bd);
        newBlock.verify();
        blockCacheService.cacheBlock(newBlock);
        BlockHeaderEvent event = new BlockHeaderEvent();
        event.setEventBody(newBlock.getHeader());
        eventBroadcaster.broadcastAndCache(event);
    }

    /**
     * CoinBase transaction & Punish transaction
     *
     * @param bestBlock
     * @param txList
     * @param self
     */
    private void addConsensusTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) {
        punishTx(bestBlock, txList, self);
        coinBaseTx(txList, self);
        CoinBaseTransaction cbTx = new CoinBaseTransaction();
    }

    private void coinBaseTx(List<Transaction> txList, PocMeetingMember self) {
        CoinTransferData data = new CoinTransferData();
        data.setFee(Na.ZERO);
        List<ConsensusReward> rewardList = calcReward();
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
        try {
            validateResult = ledgerService.verifyAndCacheTx(tx);
        } catch (NulsException e) {
            Log.error(e);
        }
        if (validateResult.isFailed()) {
            throw new NulsRuntimeException(ErrorCode.CONSENSUS_EXCEPTION);
        }
        txList.add(0, tx);
    }

    private List<ConsensusReward> calcReward() {
        ConsensusGroup cg = this.currentRound.getConsensusGroup();


        return null;
    }

    private void punishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) {
        boolean b = redPunishTx(bestBlock, txList, self);
        if (!b) {
            yellowPunishTx(bestBlock, txList, self);
        }
    }

    private boolean redPunishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) {
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
            lastBlockRoundData.parse(bestBlock.getExtend());
        } catch (NulsException e) {
            Log.error(e);
        }
        boolean punish = self.getIndexOfRound() == 1 && lastBlockRoundData.getPackingIndexOfRound() != lastBlockRoundData.getConsensusMemberCount();
        punish = punish || (self.getIndexOfRound() > 1 && self.getIndexOfRound() != (lastBlockRoundData.getPackingIndexOfRound() + 1));
        if (!punish) {
            return;
        }
        PocMeetingMember previous = currentRound.getMember(self.getIndexOfRound() - 1);
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
        PocMeetingRound round = new PocMeetingRound(this.currentRound);
        do {
            if (bestBlock.getHeader().getHeight() == 1) {
                round.setStartTime(this.context.getGenesisBlock().getHeader().getTime() + 10000L);
                break;
            }
            BlockRoundData lastRoundData = null;
            try {
                lastRoundData = new BlockRoundData(bestBlock.getExtend());
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
        list.addAll(consensusCacheService.getCachedAgentList(ConsensusStatusEnum.IN));
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

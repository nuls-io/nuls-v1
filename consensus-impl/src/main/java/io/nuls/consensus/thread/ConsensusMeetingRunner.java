/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.thread;

import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusAgentImpl;
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
import io.nuls.consensus.event.notice.PackedBlockNotice;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.impl.PocBlockService;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.BlockInfo;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.consensus.utils.DistributedBlockInfoRequestUtils;
import io.nuls.consensus.utils.TxComparator;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;
import org.spongycastle.util.Times;

import java.io.IOException;
import java.util.*;

/**
 * @author Niels
 * @date 2017/12/15
 */
public class ConsensusMeetingRunner implements Runnable {
    private static final int MIN_NODE_COUNT = 1;
    private NulsContext context = NulsContext.getInstance();
    public static final String THREAD_NAME = "Consensus-Meeting";
    private static final ConsensusMeetingRunner INSTANCE = new ConsensusMeetingRunner();
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();
    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();
    private PocBlockService pocBlockService = PocBlockService.getInstance();
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private boolean running = false;
    private ConsensusManager consensusManager = ConsensusManager.getInstance();
    private ConfirmingTxCacheManager confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();
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
                boolean b = checkCondition();
                if (b) {
                    nextRound();
                } else {
                    Thread.sleep(1000L);
                }
            } catch (Exception e) {
                Log.error(e.getMessage());
                try {
                    startMeeting();
                } catch (Exception e1) {
                    Log.error(e1.getMessage());
                }
            }
        }
    }

    private boolean checkCondition() {
        boolean result;
        do {
            List<Node> nodes = networkService.getAvailableNodes();
            result = nodes != null && nodes.size() >= MIN_NODE_COUNT;
            if (!result) {
                break;
            }
            result = (TimeService.currentTimeMillis() - context.getBestBlock().getHeader().getTime()) <= 1000L;
            if (!result) {
                result = checkBestHash();
            }
//            if (!result) {
//                break;
//            }
//            result = this.consensusManager.getConsensusStatusInfo() != null;
//            result = result && ConsensusStatusEnum.IN.getCode() == consensusManager.getConsensusStatusInfo().getStatus();
        } while (false);
        return result;
    }

    private BlockInfo lastBlockInfo;

    private boolean checkBestHash() {
        boolean result = true;
        if (null != lastBlockInfo) {
            result = checkBestHash(lastBlockInfo);
        }
        if (!result) {
            return result;
        }
        BlockInfo blockInfo = DistributedBlockInfoRequestUtils.getInstance().request(-1);
        if (blockInfo == null || blockInfo.getBestHash() == null) {
            return false;
        }
        result = checkBestHash(blockInfo);
        lastBlockInfo = blockInfo;
        return result;
    }

    private boolean checkBestHash(BlockInfo blockInfo) {
        boolean result = blockInfo.getBestHeight() <= context.getBestBlock().getHeader().getHeight();
        if (!result) {
            return result;
        }
        Block localBlock = blockService.getBlock(blockInfo.getBestHeight());
        result = null != localBlock &&
                blockInfo.getBestHash().getDigestHex()
                        .equals(localBlock.getHeader().getHash().getDigestHex());
        return result;
    }

    private void nextRound() {
        if (this.consensusManager.getConsensusStatusInfo() == null || this.consensusManager.getConsensusStatusInfo().getAddress() == null) {
            return;
        }
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
        while (currentRound.getEndTime() < TimeService.currentTimeMillis()) {
            long time = TimeService.currentTimeMillis() - currentRound.getStartTime();
            long roundTime = currentRound.getEndTime() - currentRound.getStartTime();
            long index = time / roundTime;
            long startTime = currentRound.getStartTime() + index * roundTime;
            currentRound.setStartTime(startTime);
        }
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
            if (ca.getAddress().equals(consensusManager.getConsensusStatusInfo().getAddress())) {
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
            if (cd.getExtend().getDelegateAddress().equals(consensusManager.getConsensusStatusInfo().getAddress())) {
                myDelegateList.add(cd);
                agentTotalDeposit = agentTotalDeposit.add(cd.getExtend().getDeposit());
            }
        }
        currentRound.setTotalDeposit(totalDeposit);
        currentRound.setAgentTotalDeposit(agentTotalDeposit);
        cg.setDelegateList(myDelegateList);
        currentRound.setConsensusGroup(cg);
        if (ConsensusStatusEnum.IN.getCode() == consensusManager.getConsensusStatusInfo().getStatus()) {
            startMeeting();
        }
    }

    private void startMeeting() {
        PocMeetingRound current = consensusManager.getCurrentRound();
        if (null == current || current.getMember(consensusManager.getConsensusStatusInfo().getAddress()) == null) {
            this.nextRound();
            return;
        }
        PocMeetingMember self = current.getMember(consensusManager.getConsensusStatusInfo().getAddress());
        self.setCreditVal(calcCreditVal());
        long timeUnit = 100L;
        while (TimeService.currentTimeMillis() <= (self.getPackTime() - timeUnit)) {
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
        long blockCount = pocBlockService.getBlockCount(consensusManager.getConsensusStatusInfo().getAddress(), roundStart, consensusManager.getCurrentRound().getIndex() - 1);
        long sumRoundVal = pocBlockService.getSumOfRoundIndexOfYellowPunish(consensusManager.getConsensusStatusInfo().getAddress(), consensusManager.getCurrentRound().getIndex() - PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT, consensusManager.getCurrentRound().getIndex() - 1);
        double ability = blockCount / PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (consensusManager.getCurrentRound().getIndex() == 0) {
            return 1;
        }
        double penalty = (PocConsensusConstant.CREDIT_MAGIC_NUM * sumRoundVal) / (PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT * PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);
        return ability - penalty;
    }

    private void packing(PocMeetingMember self) {
        Block bestBlock = context.getBestBlock();
        List<Transaction> txList = txCacheManager.getTxList();
        txList.sort(new TxComparator());
        BlockData bd = new BlockData();
        bd.setHeight(bestBlock.getHeader().getHeight() + 1);
        bd.setPreHash(bestBlock.getHeader().getHash());
        BlockRoundData roundData = new BlockRoundData();
        roundData.setRoundIndex(consensusManager.getCurrentRound().getIndex());
        roundData.setConsensusMemberCount(consensusManager.getCurrentRound().getMemberCount());
        roundData.setPackingIndexOfRound(self.getIndexOfRound());
        roundData.setRoundStartTime(consensusManager.getCurrentRound().getStartTime());
        bd.setRoundData(roundData);
        List<Integer> outTxList = new ArrayList<>();
        List<NulsDigestData> hashList = new ArrayList<>();
        for (int i = 0; i < txList.size(); i++) {
            Transaction tx = txList.get(i);
            ValidateResult result = tx.verify();
            if (result.isFailed()) {
                outTxList.add(i);
                continue;
            }
            try {
                ledgerService.approvalTx(tx);
            } catch (Exception e) {
                Log.error(e);
                outTxList.add(i);
                continue;
            }
            confirmingTxCacheManager.putTx(tx);
        }
        txCacheManager.removeTx(hashList);
        for (int i = outTxList.size() - 1; i >= 0; i--) {
            txList.remove(i);
        }
        addConsensusTx(bestBlock, txList, self);
        bd.setTxList(txList);
        Block newBlock = ConsensusTool.createBlock(bd);
        ValidateResult result = newBlock.verify();
        if (result.isFailed()) {
            Log.error("packing block error" + result.getMessage());
            return;
        }
        blockCacheManager.cacheBlockHeader(newBlock.getHeader(), null);
        blockCacheManager.cacheBlock(newBlock);
        BlockHeaderEvent event = new BlockHeaderEvent();
        event.setEventBody(newBlock.getHeader());
        eventBroadcaster.broadcastAndCache(event, false);
        PackedBlockNotice notice = new PackedBlockNotice();
        notice.setEventBody(newBlock.getHeader());
        eventBroadcaster.publishToLocal(notice);
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
        CoinBaseTransaction tx;
        try {
            tx = new CoinBaseTransaction(data, null);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        tx.setFee(Na.ZERO);
        tx.setHash(NulsDigestData.calcDigestData(tx));
        tx.setSign(accountService.signData(tx.getHash(), PocConsensusConstant.DEFAULT_WALLET_PASSWORD));
        ValidateResult validateResult = tx.verify();
        tx.setStatus(TxStatusEnum.AGREED);
        confirmingTxCacheManager.putTx(tx);
        if (null == validateResult || validateResult.isFailed()) {
            throw new NulsRuntimeException(ErrorCode.CONSENSUS_EXCEPTION);
        }
        txList.add(0, tx);
    }

    private List<ConsensusReward> calcReward(List<Transaction> txList) {
        List<ConsensusReward> rewardList = new ArrayList<>();
        if (this.consensusManager.getCurrentRound().getTotalDeposit().getValue() == 0) {
            ConsensusGroup cg = this.consensusManager.getCurrentRound().getConsensusGroup();
            long totalFee = 0;
            for (Transaction tx : txList) {
                totalFee += tx.getFee().getValue();
            }
            if(totalFee==0L){
                return rewardList;
            }
            double caReward = totalFee ;
            Consensus<Agent> ca = cg.getAgentConsensus();
            ConsensusReward agentReword = new ConsensusReward();
            agentReword.setAddress(ca.getAddress());
            agentReword.setReward(Na.valueOf((long) caReward));
            rewardList.add(agentReword);
            return rewardList;
        }
        ConsensusGroup cg = this.consensusManager.getCurrentRound().getConsensusGroup();
        long totalFee = 0;
        for (Transaction tx : txList) {
            totalFee += tx.getFee().getValue();
        }
        double total = totalFee + PocConsensusConstant.ANNUAL_INFLATION.getValue() *
                (PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * this.consensusManager.getCurrentRound().getMemberCount()) / PocConsensusConstant.BLOCK_COUNT_OF_YEAR
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
        redPunishTx(bestBlock, txList);
        yellowPunishTx(bestBlock, txList, self);
    }

    private void redPunishTx(Block bestBlock, List<Transaction> txList) {
        //todo check it
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
            tx.setSign(accountService.signData(tx.getHash(), PocConsensusConstant.DEFAULT_WALLET_PASSWORD));
            txList.add(tx);
        }
    }

    private void yellowPunishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) {
        //todo check it
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
        punishTx.setSign(accountService.signData(punishTx.getHash(), PocConsensusConstant.DEFAULT_WALLET_PASSWORD));
        txList.add(punishTx);
    }

    private PocMeetingRound calcRound() {
        PocMeetingRound round = new PocMeetingRound(this.consensusManager.getCurrentRound());
        Block bestBlock = context.getBestBlock();
        BlockRoundData lastRoundData;
        try {
            lastRoundData = new BlockRoundData(bestBlock.getHeader().getExtend());
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        if (round.getPreviousRound() == null) {
            while (true) {
                if (lastRoundData.getPackingIndexOfRound() == lastRoundData.getConsensusMemberCount() ||
                        lastRoundData.getRoundEndTime() <= TimeService.currentTimeMillis()) {
                    break;
                }
                try {
                    bestBlock = context.getBestBlock();
                    lastRoundData = new BlockRoundData(bestBlock.getHeader().getExtend());
                } catch (NulsException e) {
                    Log.error(e);
                }
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
            PocMeetingRound preRound = new PocMeetingRound(null);
            preRound.setIndex(lastRoundData.getRoundIndex());
            preRound.setStartTime(lastRoundData.getRoundStartTime());
            preRound.setMemberCount(lastRoundData.getConsensusMemberCount());
            round.setPreviousRound(preRound);
        }
        round.setStartTime(round.getPreviousRound().getEndTime());
        round.setIndex(lastRoundData.getRoundIndex() + 1);
        return round;
    }

    private List<Consensus<Agent>> getDefaultSeedList() throws IOException {
        List<Consensus<Agent>> seedList = new ArrayList<>();
        if (consensusManager.getSeedNodeList() == null) {
            return seedList;
        }
        for (String address : consensusManager.getSeedNodeList()) {
            Consensus<Agent> member = new ConsensusAgentImpl();
            member.setAddress(address);
            Agent agent = new Agent();
            agent.setDelegateAddress(address);
            agent.setStartTime(0);
            agent.setIntroduction("seed");
            agent.setCommissionRate(0);
            agent.setStatus(ConsensusStatusEnum.IN.getCode());
            agent.setSeed(true);
            agent.setDeposit(Na.ZERO);
            member.setExtend(agent);
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

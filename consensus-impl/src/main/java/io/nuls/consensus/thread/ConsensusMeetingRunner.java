/**
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
package io.nuls.consensus.thread;

import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.OrphanTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusAgentImpl;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.YellowPunishData;
import io.nuls.consensus.entity.block.BlockData;
import io.nuls.consensus.entity.block.BlockRoundData;
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
import io.nuls.consensus.service.impl.PocBlockService;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.BlockInfo;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.consensus.utils.DistributedBlockInfoRequestUtils;
import io.nuls.consensus.utils.TxTimeComparator;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.params.OperationType;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;

import java.io.IOException;
import java.util.*;

/**
 * @author Niels
 * @date 2017/12/15
 */
public class ConsensusMeetingRunner implements Runnable {
    private static final int MIN_NODE_COUNT = 2;
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
    private OrphanTxCacheManager orphanTxCacheManager = OrphanTxCacheManager.getInstance();
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
        boolean result = (TimeService.currentTimeMillis() - context.getBestBlock().getHeader().getTime()) <= 1000L;
        if (!result) {
            while (checkBestHash()) {
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
        }
        while (running) {
            try {
                boolean b = checkCondition();
                if (b) {
                    nextRound();
                } else {
                    Thread.sleep(10000L);
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
        List<Node> nodes = networkService.getAvailableNodes();
        boolean result = nodes != null && nodes.size() >= MIN_NODE_COUNT;
        return result;
    }

    private boolean checkBestHash() {
        BlockInfo blockInfo;
        try {
            blockInfo = DistributedBlockInfoRequestUtils.getInstance().request(-1);
        } catch (Exception e) {
            return false;
        }
        if (blockInfo == null || blockInfo.getBestHash() == null) {
            return false;
        }
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

    private void nextRound() throws NulsException, IOException {
        consensusManager.initConsensusStatusInfo();
        PocMeetingRound currentRound = calcRound();
        consensusManager.setCurrentRound(currentRound);
        while (TimeService.currentTimeMillis() < (currentRound.getStartTime())) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
        boolean imIn = consensusManager.isPartakePacking();
        List<Consensus<Agent>> list = calcConsensusAgentList();
        currentRound.setMemberCount(list.size());
        while (currentRound.getEndTime() < TimeService.currentTimeMillis()) {
            long time = TimeService.currentTimeMillis() - currentRound.getStartTime();
            long roundTime = currentRound.getEndTime() - currentRound.getStartTime();
            long index = time / roundTime;
            long startTime = currentRound.getStartTime() + index * roundTime;
            currentRound.setStartTime(startTime);
        }

        Map<String, List<Consensus<Delegate>>> delegateMap = new HashMap<>();
        List<Consensus<Delegate>> delegateList = consensusCacheManager.getCachedDelegateList();
        Na totalDeposit = Na.ZERO;
        for (Consensus<Delegate> cd : delegateList) {
            List<Consensus<Delegate>> sonList = delegateMap.get(cd.getExtend().getDelegateAddress());
            if (null == sonList) {
                sonList = new ArrayList<>();
            }
            sonList.add(cd);
            delegateMap.put(cd.getExtend().getDelegateAddress(), sonList);
            totalDeposit = totalDeposit.add(cd.getExtend().getDeposit());
        }
        List<PocMeetingMember> memberList = new ArrayList<>();
        for (Consensus<Agent> ca : list) {
            boolean isSeed = ca.getExtend().getSeed();
            if (!isSeed && ca.getExtend().getDeposit().isLessThan(PocConsensusConstant.AGENT_DEPOSIT_LOWER_LIMIT)) {
                continue;
            }
            PocMeetingMember mm = new PocMeetingMember();
            mm.setAgentConsensus(ca);
            mm.setDelegateList(delegateMap.get(ca.getAddress()));
            if (!isSeed && (mm.getDelegateList() == null || mm.getDelegateList().size() > PocConsensusConstant.MAX_ACCEPT_NUM_OF_DELEGATE)) {
                continue;
            }
            mm.calcDeposit();
            if (!isSeed && mm.getTotolEntrustDeposit().isLessThan(PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT)) {
                continue;
            }
            mm.setRoundIndex(currentRound.getIndex());
            mm.setAddress(ca.getAddress());
            mm.setPackerAddress(ca.getExtend().getAgentAddress());
            mm.setRoundStartTime(currentRound.getStartTime());
            memberList.add(mm);
            totalDeposit = totalDeposit.add(ca.getExtend().getDeposit());
        }
        Collections.sort(memberList);
        currentRound.setMemberList(memberList);
        currentRound.setTotalDeposit(totalDeposit);
        if (imIn) {
            startMeeting();
        }
    }

    private void startMeeting() throws NulsException, IOException {
        PocMeetingRound current = consensusManager.getCurrentRound();
        if (null == current || null == consensusManager.getConsensusStatusInfo().getAccount() || current.getMember(consensusManager.getConsensusStatusInfo().getAccount().getAddress().toString()) == null) {
            this.nextRound();
            return;
        }
        PocMeetingMember self = current.getMember(consensusManager.getConsensusStatusInfo().getAccount().getAddress().toString());
        self.setCreditVal(calcCreditVal());
        long timeUnit = 1000L;
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
        long blockCount = pocBlockService.getBlockCount(consensusManager.getConsensusStatusInfo().getAccount().getAddress().toString(), roundStart, consensusManager.getCurrentRound().getIndex() - 1);
        long sumRoundVal = pocBlockService.getSumOfRoundIndexOfYellowPunish(consensusManager.getConsensusStatusInfo().getAccount().getAddress().toString(), consensusManager.getCurrentRound().getIndex() - PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT, consensusManager.getCurrentRound().getIndex() - 1);
        double ability = blockCount / PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (consensusManager.getCurrentRound().getIndex() == 0) {
            return 1;
        }
        double penalty = (PocConsensusConstant.CREDIT_MAGIC_NUM * sumRoundVal) / (PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT * PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);
        return ability - penalty;
    }

    private void packing(PocMeetingMember self) throws NulsException, IOException {
        Block bestBlock = context.getBestBlock();
        List<Transaction> txList = txCacheManager.getTxList();
        txList.sort(new TxTimeComparator());
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
        List<NulsDigestData> outHashList = new ArrayList<>();
        List<NulsDigestData> hashList = new ArrayList<>();
        long totalSize = 0L;
        for (int i = 0; i < txList.size(); i++) {
            Transaction tx = txList.get(i);
            totalSize += tx.size();
            if (totalSize >= PocConsensusConstant.MAX_BLOCK_SIZE) {
                break;
            }
            outHashList.add(tx.getHash());
            ValidateResult result = tx.verify();
            if (result.isFailed()) {
                Log.error(result.getMessage());
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
        txCacheManager.removeTx(outHashList);
        if (totalSize < PocConsensusConstant.MAX_BLOCK_SIZE) {
            addOrphanTx(txList, totalSize);
        }
        addConsensusTx(bestBlock, txList, self);
        bd.setTxList(txList);
        Block newBlock = ConsensusTool.createBlock(bd, consensusManager.getConsensusStatusInfo().getAccount());
        ValidateResult result = newBlock.verify();
        if (result.isFailed()) {
            Log.warn("packing block error" + result.getMessage());
            for (Transaction tx : newBlock.getTxs()) {
                if (tx.getType() == TransactionConstant.TX_TYPE_COIN_BASE) {
                    continue;
                }
                ledgerService.rollbackTx(tx);
            }
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

    private void addOrphanTx(List<Transaction> txList, long totalSize) {
        List<Transaction> orphanTxList = orphanTxCacheManager.getTxList();
        if (null == orphanTxList || orphanTxList.isEmpty()) {
            return;
        }
        txList.sort(new TxTimeComparator());
        List<NulsDigestData> outHashList = new ArrayList<>();
        for (Transaction tx : orphanTxList) {
            totalSize += tx.size();
            if (totalSize >= PocConsensusConstant.MAX_BLOCK_SIZE) {
                break;
            }
            ValidateResult result = tx.verify();
            if (result.isFailed()) {
                Log.error(result.getMessage());
                continue;
            }
            try {
                ledgerService.approvalTx(tx);
            } catch (Exception e) {
                Log.error(result.getMessage());
                Log.error(e);
                continue;
            }
            confirmingTxCacheManager.putTx(tx);
            txList.add(tx);
            outHashList.add(tx.getHash());
        }
        orphanTxCacheManager.removeTx(outHashList);
    }


    /**
     * CoinBase transaction & Punish transaction
     *
     * @param bestBlock local highest block
     * @param txList    all tx of block
     * @param self      agent meeting data
     */
    private void addConsensusTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) throws NulsException, IOException {
        punishTx(bestBlock, txList, self);
        coinBaseTx(txList, self);
    }

    private void coinBaseTx(List<Transaction> txList, PocMeetingMember self) throws NulsException, IOException {
        CoinTransferData data = new CoinTransferData(OperationType.COIN_BASE);
        data.setFee(Na.ZERO);
        List<ConsensusReward> rewardList = calcReward(txList, self);
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
        tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), consensusManager.getConsensusStatusInfo().getAccount(), NulsContext.CACHED_PASSWORD_OF_WALLET).serialize());
        ValidateResult validateResult = tx.verify();
        confirmingTxCacheManager.putTx(tx);
        if (null == validateResult || validateResult.isFailed()) {
            throw new NulsRuntimeException(ErrorCode.CONSENSUS_EXCEPTION);
        }
        try {
            ledgerService.approvalTx(tx);
        } catch (NulsException e) {
            throw new NulsRuntimeException(e);
        }
        txList.add(0, tx);
    }

    private List<ConsensusReward> calcReward(List<Transaction> txList, PocMeetingMember self) {
        List<ConsensusReward> rewardList = new ArrayList<>();
        Consensus<Agent> ca = self.getAgentConsensus();
        if (ca.getExtend().getSeed()) {
            long totalFee = 0;
            for (Transaction tx : txList) {
                totalFee += tx.getFee().getValue();
            }
            if (totalFee == 0L) {
                return rewardList;
            }
            double caReward = totalFee;
            ConsensusReward agentReword = new ConsensusReward();
            agentReword.setAddress(ca.getAddress());
            agentReword.setReward(Na.valueOf((long) caReward));
            rewardList.add(agentReword);
            return rewardList;
        }
        long totalFee = 0;
        for (Transaction tx : txList) {
            totalFee += tx.getFee().getValue();
        }
        double total = totalFee + DoubleUtils.mul(this.consensusManager.getCurrentRound().getMemberCount(), PocConsensusConstant.BLOCK_REWARD.getValue());
        ConsensusReward agentReword = new ConsensusReward();
        agentReword.setAddress(ca.getAddress());
        double caReward = DoubleUtils.mul(total, DoubleUtils.div(ca.getExtend().getDeposit().getValue(), this.consensusManager.getCurrentRound().getTotalDeposit().getValue()));
        caReward = caReward
                + DoubleUtils.mul(total, DoubleUtils.mul(DoubleUtils.div((this.consensusManager.getCurrentRound().getTotalDeposit().getValue() - ca.getExtend().getDeposit().getValue()), this.consensusManager.getCurrentRound().getTotalDeposit().getValue()
        ), DoubleUtils.round(ca.getExtend().getCommissionRate()/100, 2)));
        agentReword.setReward(Na.valueOf((long) caReward));
        Map<String, ConsensusReward> rewardMap = new HashMap<>();
        rewardMap.put(ca.getAddress(), agentReword);
        double delegateCommissionRate = DoubleUtils.div((100 - ca.getExtend().getCommissionRate()), 100, 2);
        for (Consensus<Delegate> cd : self.getDelegateList()) {
            double reward =
                    DoubleUtils.mul(DoubleUtils.mul(total, delegateCommissionRate),
                            DoubleUtils.div(cd.getExtend().getDeposit().getValue(),
                                    this.consensusManager.getCurrentRound().getTotalDeposit().getValue()));

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

    private void punishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) throws NulsException, IOException {
        redPunishTx(bestBlock, txList);
        yellowPunishTx(bestBlock, txList, self);
    }

    private void redPunishTx(Block bestBlock, List<Transaction> txList) throws NulsException, IOException {
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
            tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), consensusManager.getConsensusStatusInfo().getAccount(), NulsContext.CACHED_PASSWORD_OF_WALLET).serialize());
            txList.add(tx);
        }
    }

    private void yellowPunishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) throws NulsException, IOException {
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
        punishTx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(punishTx.getHash(), consensusManager.getConsensusStatusInfo().getAccount(), NulsContext.CACHED_PASSWORD_OF_WALLET).serialize());
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
            agent.setAgentAddress(address);
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
        list.addAll(consensusCacheManager.getCachedAgentList(ConsensusStatusEnum.WAITING));
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

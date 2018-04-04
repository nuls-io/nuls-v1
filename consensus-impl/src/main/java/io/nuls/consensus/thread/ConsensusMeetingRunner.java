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

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.OrphanTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.MaintenanceStatus;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.YellowPunishData;
import io.nuls.consensus.entity.block.BlockData;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.consensus.event.notice.PackedBlockNotice;
import io.nuls.consensus.manager.BlockManager;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.manager.RoundManager;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.consensus.utils.TxTimeComparator;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBroadcaster;
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
    private static final int MIN_NODE_COUNT = 1;
    private NulsContext context = NulsContext.getInstance();
    public static final String THREAD_NAME = "Consensus-Meeting";
    private static final ConsensusMeetingRunner INSTANCE = new ConsensusMeetingRunner();
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private BlockManager blockManager = BlockManager.getInstance();
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();
    private OrphanTxCacheManager orphanTxCacheManager = OrphanTxCacheManager.getInstance();
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private boolean running = false;
    private boolean hasPacking = false;
    private ConsensusManager consensusManager = ConsensusManager.getInstance();
    private RoundManager packingRoundManager = RoundManager.getPackingRoundManager();
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
        //todo
        packingRoundManager.init();
        //wait the network synchronize complete and wait MeetingRound ready.
        waitReady();

        while (running) {
            try {
                doWork();
            } catch (Exception e) {
                Log.error(e);
                Log.info("consensus throw error : " + e.getMessage());
            }
        }
    }

    private void doWork() {

        PocMeetingRound round = packingRoundManager.getCurrentRound();

        long nowTime = TimeService.currentTimeMillis();
        //check current round is end
        if (nowTime >= round.getEndTime()) {
            resetCurrentMeetingRound();
            return;
        }

        //current is legal work
        if (!checkIsLegal()) {
            return;
        }

        //check i am is a consensus node
        Account myAccount = round.getLocalPacker();
        if (myAccount == null) {
            return;
        }
        PocMeetingMember member = round.getMember(myAccount.getAddress().getBase58());
        if (!hasPacking && member.getPackStartTime() <= TimeService.currentTimeMillis()) {
            packing(member, round);
            hasPacking = true;
        }

    }

    private void packing(PocMeetingMember self, PocMeetingRound round) {
        try {
            boolean needCheckAgain = waitReceiveNewestBlock(self, round);

            Block newBlock = doPacking(self, round, PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000 / 5);

            if (needCheckAgain && hasReceiveNewestBlock(self, round)) {
                Block realBestBlock = blockManager.getBlock(newBlock.getHeader().getHeight());
                List<NulsDigestData> txHashList = realBestBlock.getTxHashList();
                for (Transaction transaction : newBlock.getTxs()) {
                    if (transaction.getType() == TransactionConstant.TX_TYPE_COIN_BASE) {
                        continue;
                    }
                    if (txHashList.contains(transaction.getHash())) {
                        continue;
                    }
                    orphanTxCacheManager.putTx(transaction);
                }
                newBlock = doPacking(self, round, self.getPackEndTime() - TimeService.currentTimeMillis());
            }
            if (null == newBlock) {
                return;
            }
            //todo info to debug
            Log.info("produce block:" + newBlock.getHeader().getHash() + ",\nheight(" + newBlock.getHeader().getHeight() + "),round(" + round.getIndex() + "),index(" + self.getIndexOfRound() + "),roundStart:" + round.getStartTime());
            BlockLog.info("produce block height:" + newBlock.getHeader().getHeight() + ", preHash:" + newBlock.getHeader().getPreHash() + " , hash:" + newBlock.getHeader().getHash() + ", address:" + newBlock.getHeader().getPackingAddress());
            broadcastNewBlock(newBlock);

        } catch (NulsException e) {
            Log.error(e);
        } catch (IOException e) {
            Log.error(e);
        }
    }

    private boolean waitReceiveNewestBlock(PocMeetingMember self, PocMeetingRound round) {

        long time = TimeService.currentTimeMillis();
        long timeout = PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000 / 2;

        boolean hasReceiveNewestBlock = false;

        try {
            while (!hasReceiveNewestBlock) {
                hasReceiveNewestBlock = hasReceiveNewestBlock(self, round);
                if (hasReceiveNewestBlock) {
                    long sleepTime = time + timeout - TimeService.currentTimeMillis();
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                    break;
                }
                Thread.sleep(500l);
                if (TimeService.currentTimeMillis() - time >= timeout) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Log.error(e);
        }

        return !hasReceiveNewestBlock;
    }

    private boolean hasReceiveNewestBlock(PocMeetingMember self, PocMeetingRound round) {
        Block bestBlock = getBestBlock();
        String packingAddress = bestBlock.getHeader().getPackingAddress();

        int thisIndex = self.getIndexOfRound();

        String preBlockPackingAddress = null;

        if (thisIndex == 1) {
            PocMeetingRound preRound = round.getPreRound();
            if (preRound == null) {
                //FIXME
                return true;
            }
            preBlockPackingAddress = preRound.getMember(preRound.getMemberCount()).getPackingAddress();
        } else {
            preBlockPackingAddress = round.getMember(self.getIndexOfRound()).getPackingAddress();
        }

        if (packingAddress.equals(preBlockPackingAddress)) {
            return true;
        } else {
            return false;
        }
    }

    private void broadcastNewBlock(Block newBlock) {
        confirmingTxCacheManager.putTx(newBlock.getTxs().get(0));
        blockManager.addBlock(newBlock, false, null);
        BlockHeaderEvent event = new BlockHeaderEvent();
        event.setEventBody(newBlock.getHeader());
        List<String> nodeIdList = eventBroadcaster.broadcastAndCache(event, false);
        for (String nodeId : nodeIdList) {
            BlockLog.info("send block height:" + newBlock.getHeader().getHeight() + ", node:" + nodeId);
        }
        PackedBlockNotice notice = new PackedBlockNotice();
        notice.setEventBody(newBlock.getHeader());
        eventBroadcaster.publishToLocal(notice);
    }

    private boolean checkIsLegal() {
        if (!consensusManager.isPartakePacking()) {
            return false;
        }
        List<Node> nodes = networkService.getAvailableNodes();
        if (nodes == null || nodes.size() == 0) {
            BlockMaintenanceThread.getInstance().setStatus(MaintenanceStatus.READY);
            return false;
        }
        if (nodes.size() < MIN_NODE_COUNT) {
            return false;
        }
        if (!isNetworkSynchronizeComplete()) {
            return false;
        }
        return true;
    }

    private void waitReady() {

        while (!isNetworkSynchronizeComplete()) {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }

        //read create new meeting round
        resetCurrentMeetingRound();
    }

    //network synchronize status
    private boolean isNetworkSynchronizeComplete() {
        return BlockMaintenanceThread.getInstance().getStatus() == MaintenanceStatus.SUCCESS;
    }

    private void resetCurrentMeetingRound() {
        //TODO check
        hasPacking = false;
        packingRoundManager.resetCurrentMeetingRound();
        PocMeetingRound round = packingRoundManager.getCurrentRound();
        if (round != null) {
            long myTime = 0;

            Account myAccount = round.getLocalPacker();
            if (myAccount != null) {

                PocMeetingMember member = round.getMember(myAccount.getAddress().getBase58());
                myTime = member.getPackStartTime();
            }

            System.out.println("meeting round reset , now time : " + DateUtil.convertDate(new Date(TimeService.currentTimeMillis()))
                    + " , round end time : " + DateUtil.convertDate(new Date(round.getEndTime())) + " , my time is :" + DateUtil.convertDate(new Date(myTime)));
            System.out.println("======================================");
        }
    }

    private Block doPacking(PocMeetingMember self, PocMeetingRound round, long timeout) throws NulsException, IOException {
        Block bestBlock = this.getBestBlock();
        List<Transaction> txList = txCacheManager.getTxList();
        txList.sort(TxTimeComparator.getInstance());
        BlockData bd = new BlockData();
        bd.setHeight(bestBlock.getHeader().getHeight() + 1);
        bd.setPreHash(bestBlock.getHeader().getHash());
        BlockRoundData roundData = new BlockRoundData();
        roundData.setRoundIndex(round.getIndex());
        roundData.setConsensusMemberCount(round.getMemberCount());
        roundData.setPackingIndexOfRound(self.getIndexOfRound());
        roundData.setRoundStartTime(round.getStartTime());
        bd.setRoundData(roundData);
        List<Integer> outTxList = new ArrayList<>();
        List<NulsDigestData> outHashList = new ArrayList<>();
        List<NulsDigestData> hashList = new ArrayList<>();
        long totalSize = 0L;
        for (int i = 0; i < txList.size(); i++) {
            if ((self.getPackEndTime() - TimeService.currentTimeMillis()) <= timeout) {
                break;
            }
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
            addOrphanTx(txList, totalSize, self);
        }
        addConsensusTx(bestBlock, txList, self, round);
        bd.setTxList(txList);
        Log.debug("txCount:" + txList.size());
        Block newBlock = ConsensusTool.createBlock(bd, round.getLocalPacker());
        System.out.printf("========height:" + newBlock.getHeader().getHeight() + ",time:" + DateUtil.convertDate(new Date(newBlock.getHeader().getTime())) + ",packEndTime:" +
                DateUtil.convertDate(new Date(self.getPackEndTime())));
        ValidateResult result = newBlock.verify();
        if (result.isFailed()) {
            Log.warn("packing block error:" + result.getMessage());
            for (Transaction tx : newBlock.getTxs()) {
                ledgerService.rollbackTx(tx);
            }
            return null;
        }
        return newBlock;
    }

    private void addOrphanTx(List<Transaction> txList, long totalSize, PocMeetingMember self) {
        if ((self.getPackEndTime() - TimeService.currentTimeMillis()) <= 100) {
            return;
        }
        List<Transaction> orphanTxList = orphanTxCacheManager.getTxList();
        if (null == orphanTxList || orphanTxList.isEmpty()) {
            return;
        }
        txList.sort(TxTimeComparator.getInstance());
        List<NulsDigestData> outHashList = new ArrayList<>();
        orphanTxList.sort(TxTimeComparator.getInstance());
        for (Transaction tx : orphanTxList) {
            if ((self.getPackEndTime() - TimeService.currentTimeMillis()) <= 100) {
                break;
            }
            totalSize += tx.size();
            if (totalSize >= PocConsensusConstant.MAX_BLOCK_SIZE) {
                break;
            }
            ValidateResult result = tx.verify();
            if (result.isFailed()) {
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
    private void addConsensusTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self, PocMeetingRound round) throws NulsException, IOException {
        punishTx(bestBlock, txList, self, round);
        CoinBaseTransaction coinBaseTransaction = packingRoundManager.createNewCoinBaseTx(self, txList, round);
        coinBaseTransaction.setScriptSig(accountService.createP2PKHScriptSigFromDigest(coinBaseTransaction.getHash(), round.getLocalPacker(), NulsContext.getCachedPasswordOfWallet()).serialize());
        txList.add(0, coinBaseTransaction);
    }


    private void punishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self, PocMeetingRound round) throws NulsException, IOException {
        redPunishTx(bestBlock, txList, round);
        yellowPunishTx(bestBlock, txList, self, round);
    }

    private void redPunishTx(Block bestBlock, List<Transaction> txList, PocMeetingRound round) throws NulsException, IOException {
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
            tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), round.getLocalPacker(), NulsContext.getCachedPasswordOfWallet()).serialize());
            txList.add(tx);
        }
    }

    private void yellowPunishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self, PocMeetingRound round) throws NulsException, IOException {
        BlockRoundData lastBlockRoundData = new BlockRoundData();
        try {
            lastBlockRoundData.parse(bestBlock.getHeader().getExtend());
        } catch (NulsException e) {
            Log.error(e);
        }
        // continuous blocks in the same round
        boolean ok = (self.getRoundIndex() == lastBlockRoundData.getRoundIndex()) && (self.getIndexOfRound() == (1 + lastBlockRoundData.getPackingIndexOfRound()));

        //continuous blocks between two rounds
        ok = ok || (self.getRoundIndex() == (lastBlockRoundData.getRoundIndex() + 1)
                && self.getIndexOfRound() == 1
                && lastBlockRoundData.getPackingIndexOfRound() == lastBlockRoundData.getConsensusMemberCount());

        //two rounds
        ok = ok || (self.getRoundIndex() - 1) > lastBlockRoundData.getRoundIndex();
        if (ok) {
            return;
        }
        List<Address> addressList = new ArrayList<>();
        long roundIndex = lastBlockRoundData.getRoundIndex();
        int packingIndex = 0;

        if (lastBlockRoundData.getPackingIndexOfRound() == lastBlockRoundData.getConsensusMemberCount()) {
            roundIndex++;
            packingIndex = 1;
        } else {
            packingIndex = lastBlockRoundData.getPackingIndexOfRound() + 1;
        }

        while (true) {
            PocMeetingRound tempRound;
            if (roundIndex == self.getRoundIndex()) {
                tempRound = round;
            } else if (roundIndex == lastBlockRoundData.getRoundIndex()) {
                tempRound = round.getPreRound();
                if (null == tempRound) {
                    //todo
                    System.out.println();
                    break;
                }
            } else {
                break;
            }
            if (tempRound.getIndex() > round.getIndex()) {
                break;
            }
            if (tempRound.getIndex() == round.getIndex() && packingIndex >= self.getIndexOfRound()) {
                break;
            }
            if (packingIndex > tempRound.getMemberCount()) {
                roundIndex++;
                packingIndex = 1;
                continue;
            }
            PocMeetingMember member;
            try {
                member = tempRound.getMember(packingIndex);
                if (null == member) {
                    break;
                }
            } catch (Exception e) {
                break;
            }
            packingIndex++;
            addressList.add(Address.fromHashs(member.getAgentAddress()));
        }
        if (addressList.isEmpty()) {
            return;
        }
        YellowPunishTransaction punishTx = new YellowPunishTransaction();
        YellowPunishData data = new YellowPunishData();
        data.setAddressList(addressList);
        data.setHeight(bestBlock.getHeader().getHeight() + 1);
        punishTx.setTxData(data);
        punishTx.setTime(TimeService.currentTimeMillis());
        punishTx.setFee(Na.ZERO);
        punishTx.setHash(NulsDigestData.calcDigestData(punishTx));
        punishTx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(punishTx.getHash(), round.getLocalPacker(), NulsContext.getCachedPasswordOfWallet()).serialize());
        txList.add(punishTx);
    }

    private Block getBestBlock() {
        Block block = context.getBestBlock();
        Block highestBlock = blockManager.getHighestBlock();
        if (null != highestBlock && highestBlock.getHeader().getHeight() > block.getHeader().getHeight()) {
            return highestBlock;
        }
        return block;
    }
}

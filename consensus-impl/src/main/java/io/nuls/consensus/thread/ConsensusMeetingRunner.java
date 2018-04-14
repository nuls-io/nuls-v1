/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
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
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.OrphanTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.DownloadStatus;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.block.BlockData;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.consensus.event.notice.PackedBlockNotice;
import io.nuls.consensus.manager.BlockManager;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.manager.RoundManager;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.service.intf.DownloadService;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.consensus.utils.TxTimeComparator;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.core.utils.log.ConsensusLog;
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
    public static final String THREAD_NAME = "Consensus-Meeting";
    private static final ConsensusMeetingRunner INSTANCE = new ConsensusMeetingRunner();
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private BlockManager blockManager = BlockManager.getInstance();
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);

    private DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);

    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();
    private OrphanTxCacheManager orphanTxCacheManager = OrphanTxCacheManager.getInstance();
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private boolean hasPacking = false;
    private ConsensusManager consensusManager = ConsensusManager.getInstance();
    private RoundManager packingRoundManager = RoundManager.getInstance();
    private ConfirmingTxCacheManager confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();
    private static Map<Long, RedPunishData> punishMap = new HashMap<>();

    private boolean hasInit;

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

        try {
            //wait the network synchronize complete and wait MeetingRound ready.
            waitReady();

            doWork();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void doWork() {

        PocMeetingRound round = packingRoundManager.getCurrentRound();
        BlockLog.info("packing round:" + round.toString());
        //check current round is end
        if (null == round || round.getEndTime() < TimeService.currentTimeMillis()) {
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

    public void resetConsensus() {
        ConsensusLog.info("重置共识");
        initRound();
        hasInit = false;
    }

    private void packing(PocMeetingMember self, PocMeetingRound round) {
        try {
            boolean needCheckAgain = waitReceiveNewestBlock(self, round);
            Block newBlock = doPacking(self, round);
            if (needCheckAgain && hasReceiveNewestBlock(self, round)) {
                Block realBestBlock = blockManager.getBlock(newBlock.getHeader().getHeight());
                List<NulsDigestData> txHashList = realBestBlock.getTxHashList();
                for (Transaction transaction : newBlock.getTxs()) {
                    if (transaction.getType() == TransactionConstant.TX_TYPE_COIN_BASE || transaction.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH || transaction.getType() == TransactionConstant.TX_TYPE_RED_PUNISH) {
                        continue;
                    }
                    if (txHashList.contains(transaction.getHash())) {
                        continue;
                    }
                    orphanTxCacheManager.putTx(transaction);
                }
                newBlock = doPacking(self, round);
            }
            if (null == newBlock) {
                return;
            }
            BlockLog.info("produce block height:" + newBlock.getHeader().getHeight() + ", preHash:" + newBlock.getHeader().getPreHash() + " , hash:" + newBlock.getHeader().getHash() + ", address:" + Address.fromHashs(newBlock.getHeader().getPackingAddress()));
            broadcastSmallBlock(newBlock);

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
                Thread.sleep(500L);
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
        Block bestBlock = this.blockService.getBestBlock();
        String packingAddress = Address.fromHashs(bestBlock.getHeader().getPackingAddress()).getBase58();

        int thisIndex = self.getPackingIndexOfRound();

        String preBlockPackingAddress = null;

        if (thisIndex == 1) {
            PocMeetingRound preRound = round.getPreRound();
            if (preRound == null) {
                //FIXME
                return true;
            }
            preBlockPackingAddress = preRound.getMember(preRound.getMemberCount()).getPackingAddress();
        } else {
            preBlockPackingAddress = round.getMember(self.getPackingIndexOfRound()).getPackingAddress();
        }

        if (packingAddress.equals(preBlockPackingAddress)) {
            return true;
        } else {
            return false;
        }
    }

    private void broadcastSmallBlock(Block block) {
        StringBuilder str = new StringBuilder();
        str.append("produce a Block: height:" + block.getHeader().getHeight() + ", hash:" + block.getHeader().getHash() + " ,packingAddress:" + Address.fromHashs(block.getHeader().getPackingAddress()));
        confirmingTxCacheManager.putTx(block.getTxs().get(0));
        blockManager.addBlock(block, false, null);
        SmallBlockEvent event = new SmallBlockEvent();
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setHeader(block.getHeader());
        List<NulsDigestData> txHashList = new ArrayList<>();
        str.append("txs:\n");
        for (Transaction tx : block.getTxs()) {
            str.append(tx.getHash() + ",\n");
            txHashList.add(tx.getHash());
            if (tx.getType() == TransactionConstant.TX_TYPE_COIN_BASE ||
                    tx.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH ||
                    tx.getType() == TransactionConstant.TX_TYPE_RED_PUNISH) {
                smallBlock.addConsensusTx(tx);
            }
        }
        smallBlock.setTxHashList(txHashList);
        BlockLog.info(str.toString());
        event.setEventBody(smallBlock);
        List<String> nodeIdList = eventBroadcaster.broadcastAndCache(event, false);
        for (String nodeId : nodeIdList) {
            BlockLog.info("send block height:" + block.getHeader().getHeight() + ", node:" + nodeId);
        }
        PackedBlockNotice notice = new PackedBlockNotice();
        notice.setEventBody(block.getHeader());
        eventBroadcaster.publishToLocal(notice);
    }

    private boolean checkIsLegal() {
        if (!consensusManager.isPartakePacking()) {
            return false;
        }
        Collection<Node> nodes = networkService.getAvailableNodes();
        if (nodes == null || nodes.size() == 0 || nodes.size() < PocConsensusConstant.ALIVE_MIN_NODE_COUNT) {
            BlockLog.info("wait for available nodes,count:" + nodes.size());
            return false;
        }
        if (!isNetworkSynchronizeComplete()) {
            return false;
        }
        return true;
//        NetworkNewestBlockInfos infos = DownloadProcessor.getInstance().getNetworkNewestBlock();
//        Block bestBlock = blockService.getBestBlock();
//        return infos.getNetBestHeight()<= bestBlock.getHeader().getHeight();
    }

    private void waitReady() {

        while (!isNetworkSynchronizeComplete()) {
            try {
                Thread.sleep(1000L);

                BlockLog.info("wait for network synchronize complete,status:" + downloadService.getStatus());
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }

        if (!hasInit) {
            initRound();
            hasInit = true;
        }
    }

    private void initRound() {

        ConsensusLog.info("初始化共识");

        packingRoundManager.clear();
        packingRoundManager.init();

        ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();
        consensusCacheManager.clear();
        consensusCacheManager.init();

        //read create new meeting round
        resetCurrentMeetingRound();
    }

    //network synchronize status
    private boolean isNetworkSynchronizeComplete() {
        return downloadService.getStatus() == DownloadStatus.SUCCESS;
    }

    private void resetCurrentMeetingRound() {


        //TODO check
        hasPacking = false;
        packingRoundManager.resetCurrentMeetingRound();
        PocMeetingRound round = packingRoundManager.getCurrentRound();

        List<PocMeetingMember> list = round.getMemberList();

        StringBuilder sb = new StringBuilder();

        for (PocMeetingMember member : list) {
            sb.append("agent: " + member.getAgentAddress() + "\n");
            sb.append("packer: " + member.getPackingAddress() + "\n");
            sb.append("index: " + member.getPackingIndexOfRound() + "\n");
            sb.append("startTime: " + DateUtil.convertDate(new Date(member.getPackStartTime())) + "\n");
        }

        ConsensusLog.info("重新计算共识轮次，当前轮次 {} , 人数 {} , 开始时间 {} , 结束时间 {} , 打包者列表 :\n {}", round.getIndex(), round.getMemberCount(), DateUtil.convertDate(new Date(round.getStartTime())), DateUtil.convertDate(new Date(round.getEndTime())), sb.toString());

        if (round != null) {
            long myTime = 0;

            Account myAccount = round.getLocalPacker();
            if (myAccount != null) {

                PocMeetingMember member = round.getMember(myAccount.getAddress().getBase58());
                myTime = member.getPackStartTime();

                ConsensusLog.info("我的打包时间 {}", DateUtil.convertDate(new Date(myTime)));
            }

            System.out.println("meeting round reset , now time : " + DateUtil.convertDate(new Date(TimeService.currentTimeMillis()))
                    + " , round end time : " + DateUtil.convertDate(new Date(round.getEndTime())) + " , my time is :" + DateUtil.convertDate(new Date(myTime)));
            System.out.println("======================================");

        }
    }

    private Block doPacking(PocMeetingMember self, PocMeetingRound round) throws NulsException, IOException {

        Block bestBlock = this.blockService.getBestBlock();
        List<Transaction> allTxList = txCacheManager.getTxList();
        allTxList.sort(TxTimeComparator.getInstance());
        BlockData bd = new BlockData();
        bd.setHeight(bestBlock.getHeader().getHeight() + 1);
        bd.setPreHash(bestBlock.getHeader().getHash());
        BlockRoundData roundData = new BlockRoundData();
        roundData.setRoundIndex(round.getIndex());
        roundData.setConsensusMemberCount(round.getMemberCount());
        roundData.setPackingIndexOfRound(self.getPackingIndexOfRound());
        roundData.setRoundStartTime(round.getStartTime());

        bd.setRoundData(roundData);
        List<Integer> outTxList = new ArrayList<>();
        List<Transaction> packingTxList = new ArrayList<>();
        List<NulsDigestData> outHashList = new ArrayList<>();
        long totalSize = 0L;
        for (int i = 0; i < allTxList.size(); i++) {
            if ((self.getPackEndTime() - TimeService.currentTimeMillis()) <= 500L) {
                break;
            }
            Transaction tx = allTxList.get(i);
            tx.setBlockHeight(bd.getHeight());
            if ((totalSize + tx.size()) >= PocConsensusConstant.MAX_BLOCK_SIZE) {
                break;
            }
            outHashList.add(tx.getHash());
            ValidateResult result = tx.verify();
            if (result.isFailed()) {
                Log.error(result.getMessage());
                outTxList.add(i);
                BlockLog.info("discard tx:" + tx.getHash());
                continue;
            }
            try {
                ledgerService.approvalTx(tx);
            } catch (Exception e) {
                Log.error(e);
                outTxList.add(i);
                BlockLog.info("discard tx:" + tx.getHash());
                continue;
            }
            packingTxList.add(tx);
            totalSize += tx.size();
            confirmingTxCacheManager.putTx(tx);

        }
        txCacheManager.removeTx(outHashList);
        if (totalSize < PocConsensusConstant.MAX_BLOCK_SIZE) {
            addOrphanTx(packingTxList, totalSize, self, bd.getHeight());
        }
        addConsensusTx(bestBlock, packingTxList, self, round);
        bd.setTxList(packingTxList);
        Block newBlock = ConsensusTool.createBlock(bd, round.getLocalPacker());
        ValidateResult result = newBlock.verify();
        if (result.isFailed()) {
            BlockLog.warn("packing block error:" + result.getMessage());
            for (Transaction tx : newBlock.getTxs()) {
                ledgerService.rollbackTx(tx);
            }
            return null;
        }
        return newBlock;
    }

    private void addOrphanTx(List<Transaction> txList, long totalSize, PocMeetingMember self, long blockHeight) {
        if ((self.getPackEndTime() - TimeService.currentTimeMillis()) <= 100) {
            return;
        }
        List<Transaction> orphanTxList = orphanTxCacheManager.getTxList();
        if (null == orphanTxList || orphanTxList.isEmpty()) {
            return;
        }
        List<NulsDigestData> outHashList = new ArrayList<>();
        orphanTxList.sort(TxTimeComparator.getInstance());
        for (Transaction tx : orphanTxList) {
            tx.setBlockHeight(blockHeight);
            if ((totalSize + tx.size()) >= PocConsensusConstant.MAX_BLOCK_SIZE) {
                break;
            }
            if ((self.getPackEndTime() - TimeService.currentTimeMillis()) <= 100) {
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
            totalSize += tx.size();
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
        CoinBaseTransaction coinBaseTransaction = ConsensusTool.createCoinBaseTx(self, txList, round);
        BlockLog.info("create coinbase:" + coinBaseTransaction.getHash() + ",time:" + coinBaseTransaction.getTime());
        txList.add(0, coinBaseTransaction);
    }


    private void punishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self, PocMeetingRound round) throws NulsException, IOException {
        redPunishTx(bestBlock, txList, round);
        YellowPunishTransaction yellowPunishTransaction = ConsensusTool.createYellowPunishTx(bestBlock, self, round);
        if (null != yellowPunishTransaction) {
            StringBuilder str = new StringBuilder("create yellowPunish:" + yellowPunishTransaction.getHash() + ",time:" + yellowPunishTransaction.getTime()+"\n");
            for (Address address : yellowPunishTransaction.getTxData().getAddressList()) {
                str.append(address.toString()+",\n");
            }
            txList.add(yellowPunishTransaction);
        }
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

}

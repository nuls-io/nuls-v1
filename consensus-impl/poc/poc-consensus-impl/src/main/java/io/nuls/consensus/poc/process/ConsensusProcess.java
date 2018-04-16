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
package io.nuls.consensus.poc.process;

import io.nuls.account.entity.Account;
import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.manager.RoundManager;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.provider.ConsensusSystemProvider;
import io.nuls.consensus.poc.utils.ConsensusTool;
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
import io.nuls.network.service.NetworkService;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.poc.service.intf.ConsensusService;
import io.nuls.protocol.base.constant.PocConsensusConstant;
import io.nuls.protocol.base.entity.RedPunishData;
import io.nuls.protocol.base.entity.block.BlockData;
import io.nuls.protocol.base.entity.block.BlockRoundData;
import io.nuls.protocol.base.entity.meeting.PocMeetingMember;
import io.nuls.protocol.base.entity.meeting.PocMeetingRound;
import io.nuls.protocol.base.entity.tx.RedPunishTransaction;
import io.nuls.protocol.base.entity.tx.YellowPunishTransaction;
import io.nuls.protocol.base.event.notice.PackedBlockNotice;
import io.nuls.protocol.entity.Consensus;
import io.nuls.protocol.event.SmallBlockEvent;
import io.nuls.protocol.utils.TxTimeComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class ConsensusProcess {

    private ChainManager chainManager;
    private RoundManager roundManager;
    private TxMemoryPool txMemoryPool;

    private BlockProcess blockProcess;
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);


    private boolean hasPacking;

    public ConsensusProcess(ChainManager chainManager, RoundManager roundManager, TxMemoryPool txMemoryPool, BlockProcess blockProcess) {
        this.chainManager = chainManager;
        this.roundManager = roundManager;
        this.txMemoryPool = txMemoryPool;
        this.blockProcess = blockProcess;
    }

    public void process() {

        boolean canPackage = checkCanPackage();

        if(!canPackage || false) {
            return;
        }

        doWork();
    }

    private boolean checkCanPackage() {

        // TODO load config

        // wait consensus ready running
        if(ConsensusSystemProvider.getConsensusStatus().ordinal() <= ConsensusStatus.WAIT_START.ordinal()) {
            return false;
        }

        // check network status
//        if(networkService.getAvailableNodes().size() == 0) {
//            return false;
//        }

        return true;
    }

    private void doWork() {

        MeetingRound round = chainManager.getMasterChain().resetRound(true);

        if(round == null) {
            return;
        }

        //check i am is a consensus node
        Account myAccount = round.getLocalPacker();
        if (myAccount == null) {
            return;
        }
        MeetingMember member = round.getMember(myAccount.getAddress().getBase58());

        if (!hasPacking && member.getPackStartTime() < TimeService.currentTimeMillis() && member.getPackEndTime() > TimeService.currentTimeMillis()) {
            hasPacking = true;
            try {
                Log.info("当前网络时间： " + DateUtil.convertDate(new Date(TimeService.currentTimeMillis())) + " , 我的打包开始时间: " +
                        DateUtil.convertDate(new Date(member.getPackStartTime()))+ " , 我的打包结束时间: " +
                        DateUtil.convertDate(new Date(member.getPackEndTime())) + " , 当前轮开始时间: " +
                        DateUtil.convertDate(new Date(round.getStartTime()))+ " , 当前轮结束开始时间: " +
                        DateUtil.convertDate(new Date(round.getEndTime())));
                packing(member, round);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while(member.getPackEndTime() > TimeService.currentTimeMillis()) {
                try {
                    Thread.sleep(500l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            hasPacking = false;
        }
    }

    private void packing(MeetingMember member, MeetingRound round) throws IOException, NulsException {
        Log.info(round.toString());
        Block block = doPacking(member, round);
        boolean success = saveBlock(block);
        if(success) {
            broadcastSmallBlock(block);
        } else {
            ConsensusLog.error("make a block, but save block error");
        }
    }

    private boolean saveBlock(Block block) throws IOException {
        return blockProcess.addBlock(new BlockContainer(block, BlockContainerStatus.RECEIVED));
    }

    private void broadcastSmallBlock(Block block) {
        SmallBlockEvent event = new SmallBlockEvent();
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setHeader(block.getHeader());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (Transaction tx : block.getTxs()) {
            txHashList.add(tx.getHash());
            if (tx.getType() == TransactionConstant.TX_TYPE_COIN_BASE ||
                    tx.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH ||
                    tx.getType() == TransactionConstant.TX_TYPE_RED_PUNISH) {
                smallBlock.addConsensusTx(tx);
            }
        }
        smallBlock.setTxHashList(txHashList);

        event.setEventBody(smallBlock);
        List<String> nodeIdList = eventBroadcaster.broadcastAndCache(event, false);
        for (String nodeId : nodeIdList) {
            ConsensusLog.debug("send block height:" + block.getHeader().getHeight() + ", node:" + nodeId);
        }
        PackedBlockNotice notice = new PackedBlockNotice();
        notice.setEventBody(block.getHeader());
        eventBroadcaster.publishToLocal(notice);
    }

    private Block doPacking(MeetingMember self, MeetingRound round) throws NulsException, IOException {

        Block bestBlock = chainManager.getBestBlock();

        BlockData bd = new BlockData();
        bd.setHeight(bestBlock.getHeader().getHeight() + 1);
        bd.setPreHash(bestBlock.getHeader().getHash());
        BlockRoundData roundData = new BlockRoundData();
        roundData.setRoundIndex(round.getIndex());
        roundData.setConsensusMemberCount(round.getMemberCount());
        roundData.setPackingIndexOfRound(self.getPackingIndexOfRound());
        roundData.setRoundStartTime(round.getStartTime());

        StringBuilder str = new StringBuilder();
        str.append(self.getPackingAddress());
        str.append(" ,order:" + self.getPackingIndexOfRound());
        str.append(",packTime:" + new Date(self.getPackEndTime()));
        str.append("\n");
        ConsensusLog.debug("pack round:" + str);

        bd.setRoundData(roundData);

        List<Transaction> packingTxList = new ArrayList<>();
        List<NulsDigestData> outHashList = new ArrayList<>();

        long totalSize = 0L;

        while(true) {
            if ((self.getPackEndTime() - TimeService.currentTimeMillis()) <= 500L) {
                break;
            }
            Transaction tx = txMemoryPool.get();

            if(tx == null) {
                try {
                    Thread.sleep(100l);
                } catch (InterruptedException e) {
                    ConsensusLog.error("packaging error ", e);
                }
                continue;
            }

            if ((totalSize + tx.size()) > PocConsensusConstant.MAX_BLOCK_SIZE) {
                txMemoryPool.add(tx, false);
                break;
            }
            if(outHashList.contains(tx.getHash())) {
                continue;
            }
            outHashList.add(tx.getHash());
            ValidateResult result = tx.verify();
            if (result.isFailed()) {
                ConsensusLog.error(result.getMessage());
                continue;
            }

            tx.setBlockHeight(bd.getHeight());
            packingTxList.add(tx);

            totalSize += tx.size();
        }

        addConsensusTx(bestBlock, packingTxList, self, round);
        bd.setTxList(packingTxList);

        Block newBlock = ConsensusTool.createBlock(bd, round.getLocalPacker());

        ConsensusLog.debug("make block height:" + newBlock.getHeader().getHeight() + ",txCount: " + newBlock.getTxs().size() + ", time:" + DateUtil.convertDate(new Date(newBlock.getHeader().getTime())) + ",packEndTime:" +
                DateUtil.convertDate(new Date(self.getPackEndTime())));

        return newBlock;
    }

    /**
     * CoinBase transaction & Punish transaction
     *
     * @param bestBlock local highest block
     * @param txList    all tx of block
     * @param self      agent meeting data
     */
    private void addConsensusTx(Block bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        punishTx(bestBlock, txList, self, round);
        CoinBaseTransaction coinBaseTransaction = ConsensusTool.createCoinBaseTx(self, txList, round);
        txList.add(0, coinBaseTransaction);
    }

    private void punishTx(Block bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        redPunishTx(bestBlock, txList, round);
        YellowPunishTransaction yellowPunishTransaction = ConsensusTool.createYellowPunishTx(bestBlock, self, round);
        if (null != yellowPunishTransaction) {
            txList.add(yellowPunishTransaction);
        }
    }

    private void redPunishTx(Block bestBlock, List<Transaction> txList, MeetingRound round) throws NulsException, IOException {
        //todo check it
    }

}

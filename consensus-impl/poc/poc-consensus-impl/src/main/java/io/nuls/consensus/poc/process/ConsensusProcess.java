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
import io.nuls.account.entity.Address;
import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.manager.RoundManager;
import io.nuls.consensus.poc.protocol.constant.PocConsensusConstant;
import io.nuls.consensus.poc.protocol.context.ConsensusContext;
import io.nuls.consensus.poc.protocol.event.notice.PackedBlockNotice;
import io.nuls.consensus.poc.protocol.model.MeetingMember;
import io.nuls.consensus.poc.protocol.model.MeetingRound;
import io.nuls.consensus.poc.protocol.model.block.BlockData;
import io.nuls.consensus.poc.protocol.model.block.BlockRoundData;
import io.nuls.consensus.poc.protocol.tx.YellowPunishTransaction;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.consensus.poc.provider.BlockQueueProvider;
import io.nuls.consensus.poc.provider.ConsensusSystemProvider;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.network.service.NetworkService;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.SmallBlockEvent;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.SmallBlock;
import io.nuls.protocol.model.Transaction;

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

    private BlockQueueProvider blockQueueProvider;
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);


    private boolean hasPacking;

    public ConsensusProcess(ChainManager chainManager, RoundManager roundManager, TxMemoryPool txMemoryPool, BlockQueueProvider blockQueueProvider) {
        this.chainManager = chainManager;
        this.roundManager = roundManager;
        this.txMemoryPool = txMemoryPool;
        this.blockQueueProvider = blockQueueProvider;
    }

    public void process() {

        boolean canPackage = checkCanPackage();

        if (!canPackage || false) {
            return;
        }

        doWork();
    }

    private boolean checkCanPackage() {

        if (!ConsensusContext.isPartakePacking()) {
            return false;
        }

        // wait consensus ready running
        if (ConsensusSystemProvider.getConsensusStatus().ordinal() <= ConsensusStatus.WAIT_START.ordinal()) {
            return false;
        }

        // check network status
        if (networkService.getAvailableNodes().size() == 0) {
            return false;
        }

        return true;
    }

    private void doWork() {

        MeetingRound round = chainManager.getMasterChain().resetRound(true);

        if (round == null) {
            return;
        }

        //check i am is a consensus node
        MeetingMember member = round.getMyMember();
        if (member == null) {
            return;
        }

        if (!hasPacking && member.getPackStartTime() < TimeService.currentTimeMillis() && member.getPackEndTime() > TimeService.currentTimeMillis()) {
            hasPacking = true;
            try {
                Log.debug("当前网络时间： " + DateUtil.convertDate(new Date(TimeService.currentTimeMillis())) + " , 我的打包开始时间: " +
                        DateUtil.convertDate(new Date(member.getPackStartTime())) + " , 我的打包结束时间: " +
                        DateUtil.convertDate(new Date(member.getPackEndTime())) + " , 当前轮开始时间: " +
                        DateUtil.convertDate(new Date(round.getStartTime())) + " , 当前轮结束开始时间: " +
                        DateUtil.convertDate(new Date(round.getEndTime())));
                packing(member, round);
            } catch (Exception e) {
                Log.error(e);
            }

            while (member.getPackEndTime() > TimeService.currentTimeMillis()) {
                try {
                    Thread.sleep(500l);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
            hasPacking = false;
        }
    }

    private void packing(MeetingMember self, MeetingRound round) throws IOException, NulsException {
        Log.debug(round.toString());

        boolean needCheckAgain = waitReceiveNewestBlock(self, round);

        Block block = doPacking(self, round);

        if (needCheckAgain && hasReceiveNewestBlock(self, round)) {
            Block realBestBlock = chainManager.getBestBlock();
            if (null != realBestBlock) {
                List<NulsDigestData> txHashList = realBestBlock.getTxHashList();
                for (Transaction transaction : block.getTxs()) {
                    if (transaction.getType() == TransactionConstant.TX_TYPE_COIN_BASE || transaction.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH || transaction.getType() == TransactionConstant.TX_TYPE_RED_PUNISH) {
                        continue;
                    }
                    if (txHashList.contains(transaction.getHash())) {
                        continue;
                    }
                    txMemoryPool.add(transaction, false);
                }
                block = doPacking(self, round);
            }
        }
        if (null == block) {
            Log.error("make a null block");
            return;
        }

        boolean success = saveBlock(block);
        if (success) {
            broadcastSmallBlock(block);
        } else {
            Log.error("make a block, but save block error");
        }
    }

    private boolean waitReceiveNewestBlock(MeetingMember self, MeetingRound round) {

        long time = TimeService.currentTimeMillis();
        long timeout = ProtocolConstant.BLOCK_TIME_INTERVAL_MILLIS / 2;

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

    private boolean hasReceiveNewestBlock(MeetingMember self, MeetingRound round) {
        Block bestBlock = chainManager.getBestBlock();
        String packingAddress = Address.fromHashs(bestBlock.getHeader().getPackingAddress()).getBase58();

        int thisIndex = self.getPackingIndexOfRound();

        String preBlockPackingAddress = null;

        if (thisIndex == 1) {
            MeetingRound preRound = round.getPreRound();
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

    private boolean saveBlock(Block block) throws IOException {
        return blockQueueProvider.put(new BlockContainer(block, BlockContainerStatus.RECEIVED), true);
    }

    private void broadcastSmallBlock(Block block) {
        SmallBlockEvent event = new SmallBlockEvent();
        SmallBlock smallBlock = ConsensusTool.getSmallBlock(block);
        event.setEventBody(smallBlock);
        List<String> nodeIdList = eventBroadcaster.broadcastAndCache(event);
        for (String nodeId : nodeIdList) {
            Log.debug("send block height:" + block.getHeader().getHeight() + ", node:" + nodeId);
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
        bd.setTime(self.getPackEndTime());
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
        Log.debug("pack round:" + str);

        bd.setRoundData(roundData);

        List<Transaction> packingTxList = new ArrayList<>();
        List<NulsDigestData> outHashList = new ArrayList<>();

        long totalSize = 0L;

        while (true) {
            if ((self.getPackEndTime() - TimeService.currentTimeMillis()) <= 500L) {
                break;
            }
            Transaction tx = txMemoryPool.get();

            if (tx == null) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    Log.error("packaging error ", e);
                }
                continue;
            }

            if ((totalSize + tx.size()) > PocConsensusConstant.MAX_BLOCK_SIZE) {
                txMemoryPool.add(tx, false);
                break;
            }
            if (outHashList.contains(tx.getHash())) {
                continue;
            }
            Transaction repeatTx = ledgerService.getTx(tx.getHash());
            if (repeatTx != null) {
                continue;
            }
            ValidateResult result = ledgerService.conflictDetectTx(tx, packingTxList);
            if (result.isFailed()) {
                Log.debug(result.getMessage());
                continue;
            }
            result = tx.verify();
            if(result.isFailed()&&result.getErrorCode()== ErrorCode.ORPHAN_TX){
                AbstractCoinTransaction coinTx = (AbstractCoinTransaction) tx;
                result = coinTx.getCoinDataProvider().verifyCoinData(coinTx,packingTxList);
                if(result.isSuccess()){
                    coinTx.setSkipInputValidator(true);
                    result = coinTx.verify();
                }
            }
            if (result.isFailed()) {
                Log.debug(result.getMessage());
                continue;
            }
            outHashList.add(tx.getHash());

            tx.setBlockHeight(bd.getHeight());
            packingTxList.add(tx);

            totalSize += tx.size();
        }

        addConsensusTx(bestBlock, packingTxList, self, round);
        bd.setTxList(packingTxList);

        Block newBlock = ConsensusTool.createBlock(bd, round.getLocalPacker());

        Log.debug("make block height:" + newBlock.getHeader().getHeight() + ",txCount: " + newBlock.getTxs().size() + ", time:" + DateUtil.convertDate(new Date(newBlock.getHeader().getTime())) + ",packEndTime:" +
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
        CoinBaseTransaction coinBaseTransaction = ConsensusTool.createCoinBaseTx(self, txList, round, bestBlock.getHeader().getHeight() + 1 + PocConsensusConstant.COINBASE_UNLOCK_HEIGHT);
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
        //todo implement
    }

}

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

import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.OrphanTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.YellowPunishData;
import io.nuls.consensus.entity.block.BlockData;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.consensus.event.notice.PackedBlockNotice;
import io.nuls.consensus.manager.BlockManager;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.manager.PackingRoundManager;
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
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.TimeService;
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
    private static final int MIN_NODE_COUNT = 2;
    private NulsContext context = NulsContext.getInstance();
    public static final String THREAD_NAME = "Consensus-Meeting";
    private static final ConsensusMeetingRunner INSTANCE = new ConsensusMeetingRunner();
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private BlockManager blockManager = BlockManager.getInstance();
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();
    private OrphanTxCacheManager orphanTxCacheManager = OrphanTxCacheManager.getInstance();
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private boolean running = false;
    private ConsensusManager consensusManager = ConsensusManager.getInstance();
    private PackingRoundManager packingRoundManager = PackingRoundManager.getInstance();
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

    private Block getBestBlock() {
        Block block = context.getBestBlock();
        Block highestBlock = blockManager.getHighestBlock();
        if (null != highestBlock && highestBlock.getHeader().getHeight() > block.getHeader().getHeight()) {
            return highestBlock;
        }
        return block;
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        this.running = true;
        boolean result = (TimeService.currentTimeMillis() - this.getBestBlock().getHeader().getTime()) <= 1000L*PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND;
        if (!result) {
            while (!checkBestHash()) {
                try {
                    Thread.sleep(1000L);
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
                    Thread.sleep(1000L);
                }
            } catch (Exception e) {
                Log.error(e);
                try {
                    startMeeting();
                } catch (Exception e1) {
                    Log.error(e1);
                }
            }
        }
    }

    private boolean checkCondition() {
        List<Node> nodes = networkService.getAvailableNodes();
        boolean result = nodes != null && nodes.size() >= MIN_NODE_COUNT;
        result = result && NulsContext.getInstance().getNetBestBlockHeight() != null;
        boolean localIsSeed = packingRoundManager.isLocalHasSeed(accountService.getAccountList());
        boolean synced = ((NulsContext.getInstance().getNetBestBlockHeight() > 0 || localIsSeed) && this.getBestBlock().getHeader().getHeight() >= NulsContext.getInstance().getNetBestBlockHeight());
        result = result && synced;
        return result;
    }

    private boolean checkBestHash() {
        BlockInfo blockInfo;
        try {
            blockInfo = DistributedBlockInfoRequestUtils.getInstance().request(-1, null);
        } catch (Exception e) {
            return false;
        }
        if (blockInfo == null || blockInfo.getBestHash() == null) {
            return false;
        }
        boolean result = blockInfo.getBestHeight() <= this.getBestBlock().getHeader().getHeight();
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
        packingRoundManager.calc(getBestBlock());
        while (TimeService.currentTimeMillis() < (packingRoundManager.getCurrentRound().getStartTime())) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
        boolean imIn = consensusManager.isPartakePacking() && packingRoundManager.getCurrentRound().getLocalPacker() != null;
        if (imIn) {
            startMeeting();
        }
    }

    private void startMeeting() throws NulsException, IOException {

        PocMeetingMember self = packingRoundManager.getCurrentRound().getMember(packingRoundManager.getCurrentRound().getLocalPacker().getAddress().toString());
        long timeUnit = 2000L;
        while (TimeService.currentTimeMillis() <= (self.getPackTime() - timeUnit)) {
            try {
                Thread.sleep(timeUnit);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
        packing(self);
    }


    private void packing(PocMeetingMember self) throws NulsException, IOException {
        Block bestBlock = this.getBestBlock();
        List<Transaction> txList = txCacheManager.getTxList();
        txList.sort(TxTimeComparator.getInstance());
        BlockData bd = new BlockData();
        bd.setHeight(bestBlock.getHeader().getHeight() + 1);
        bd.setPreHash(bestBlock.getHeader().getHash());
        BlockRoundData roundData = new BlockRoundData();
        roundData.setRoundIndex(packingRoundManager.getCurrentRound().getIndex());
        roundData.setConsensusMemberCount(packingRoundManager.getCurrentRound().getMemberCount());
        roundData.setPackingIndexOfRound(self.getIndexOfRound());
        roundData.setRoundStartTime(packingRoundManager.getCurrentRound().getStartTime());
        bd.setRoundData(roundData);
        List<Integer> outTxList = new ArrayList<>();
        List<NulsDigestData> outHashList = new ArrayList<>();
        List<NulsDigestData> hashList = new ArrayList<>();
        long totalSize = 0L;
        for (int i = 0; i < txList.size(); i++) {
            if ((self.getPackTime() - TimeService.currentTimeMillis()) <= 100) {
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
        addConsensusTx(bestBlock, txList, self);
        bd.setTxList(txList);
        Log.info("txCount:" + txList.size());
        Block newBlock = ConsensusTool.createBlock(bd, packingRoundManager.getCurrentRound().getLocalPacker());
        ValidateResult result = newBlock.verify();
        if (result.isFailed()) {
            Log.warn("packing block error:" + result.getMessage());
            for (Transaction tx : newBlock.getTxs()) {
                ledgerService.rollbackTx(tx);
            }
            return;
        }
        confirmingTxCacheManager.putTx(newBlock.getTxs().get(0));
        blockManager.addBlock(newBlock, false, null);
        BlockHeaderEvent event = new BlockHeaderEvent();
        event.setEventBody(newBlock.getHeader());
        eventBroadcaster.broadcastAndCache(event, false);
        PackedBlockNotice notice = new PackedBlockNotice();
        notice.setEventBody(newBlock.getHeader());
        eventBroadcaster.publishToLocal(notice);
    }

    private void addOrphanTx(List<Transaction> txList, long totalSize, PocMeetingMember self) {
        if ((self.getPackTime() - TimeService.currentTimeMillis()) <= 100) {
            return;
        }
        List<Transaction> orphanTxList = orphanTxCacheManager.getTxList();
        if (null == orphanTxList || orphanTxList.isEmpty()) {
            return;
        }
        txList.sort(TxTimeComparator.getInstance());
        List<NulsDigestData> outHashList = new ArrayList<>();
        for (Transaction tx : orphanTxList) {
            if ((self.getPackTime() - TimeService.currentTimeMillis()) <= 100) {
                break;
            }
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
        CoinBaseTransaction coinBaseTransaction = packingRoundManager.createNewCoinBaseTx(self, txList, packingRoundManager.getCurrentRound());
        coinBaseTransaction.setScriptSig(accountService.createP2PKHScriptSigFromDigest(coinBaseTransaction.getHash(), packingRoundManager.getCurrentRound().getLocalPacker(), NulsContext.CACHED_PASSWORD_OF_WALLET).serialize());
        txList.add(0,coinBaseTransaction);
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
            tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), packingRoundManager.getCurrentRound().getLocalPacker(), NulsContext.CACHED_PASSWORD_OF_WALLET).serialize());
            txList.add(tx);
        }
    }

    private void yellowPunishTx(Block bestBlock, List<Transaction> txList, PocMeetingMember self) throws NulsException, IOException {
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
        PocMeetingRound round = packingRoundManager.getCurrentRound();
        long roundIndex = lastBlockRoundData.getRoundIndex();
        int packingIndex = 0;

        if (lastBlockRoundData.getPackingIndexOfRound() == lastBlockRoundData.getConsensusMemberCount()) {
            packingIndex = 1;
        } else {
            packingIndex = lastBlockRoundData.getPackingIndexOfRound() + 1;
        }

        while (true) {
            PocMeetingRound tempRound;
            if (roundIndex == self.getRoundIndex()) {
                tempRound = round;
            } else if (roundIndex == (self.getRoundIndex() - 1)) {
                tempRound = packingRoundManager.getPreviousRound();
            } else {
                break;
            }
            if (tempRound.getIndex() > round.getIndex()) {
                break;
            }
            if (tempRound.getIndex() == round.getIndex() && packingIndex >= self.getIndexOfRound()) {
                break;
            }
            if (packingIndex >= tempRound.getMemberCount()) {
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
        punishTx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(punishTx.getHash(), packingRoundManager.getCurrentRound().getLocalPacker(), NulsContext.CACHED_PASSWORD_OF_WALLET).serialize());
        txList.add(punishTx);
    }

}

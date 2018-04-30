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

package io.nuls.consensus.poc.container;

import io.nuls.account.entity.Address;
import io.nuls.consensus.poc.manager.RoundManager;
import io.nuls.consensus.poc.model.*;
import io.nuls.consensus.poc.protocol.constant.PocConsensusConstant;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.model.*;
import io.nuls.consensus.poc.protocol.model.MeetingMember;
import io.nuls.consensus.poc.protocol.model.MeetingRound;
import io.nuls.consensus.poc.protocol.model.block.BlockRoundData;
import io.nuls.consensus.poc.protocol.service.BlockService;
import io.nuls.consensus.poc.protocol.tx.*;
import io.nuls.consensus.poc.protocol.tx.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.entity.YellowPunishData;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.PunishLogPo;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.BlockHeader;
import io.nuls.protocol.model.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class ChainContainer implements Cloneable {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    private Chain chain;
    private RoundManager roundManager;

    public ChainContainer() {
    }

    public ChainContainer(Chain chain) {
        this.chain = chain;
        roundManager = new RoundManager(chain);
    }

    public boolean addBlock(Block block) {

        if (!chain.getEndBlockHeader().getHash().equals(block.getHeader().getPreHash()) ||
                chain.getEndBlockHeader().getHeight() + 1 != block.getHeader().getHeight()) {
            return false;
        }

        List<Block> blockList = chain.getBlockList();
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();

        List<Consensus<Agent>> agentList = chain.getAgentList();
        List<Consensus<Deposit>> depositList = chain.getDepositList();
        List<PunishLogPo> yellowList = chain.getYellowPunishList();
        List<PunishLogPo> redList = chain.getRedPunishList();

        long height = block.getHeader().getHeight();

        List<Transaction> txs = block.getTxs();
        for (Transaction tx : txs) {
            int txType = tx.getType();
            if (txType == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
                // Registered agent transaction
                // 注册代理交易
                RegisterAgentTransaction registerAgentTx = (RegisterAgentTransaction) tx;
                Consensus<Agent> ca = registerAgentTx.getTxData();

                Consensus<Agent> caAgent = ConsensusTool.copyConsensusAgent(ca);
                caAgent.setDelHeight(0L);
                caAgent.getExtend().setBlockHeight(height);
                agentList.add(caAgent);
            } else if (txType == TransactionConstant.TX_TYPE_JOIN_CONSENSUS) {
                PocJoinConsensusTransaction joinConsensusTx = (PocJoinConsensusTransaction) tx;
                Consensus<Deposit> cDeposit = joinConsensusTx.getTxData();

                Consensus<Deposit> caDeposit = ConsensusTool.copyConsensusDeposit(cDeposit);
                caDeposit.setDelHeight(0L);
                caDeposit.getExtend().setBlockHeight(height);
                depositList.add(caDeposit);

            } else if (txType == TransactionConstant.TX_TYPE_CANCEL_DEPOSIT) {

                CancelDepositTransaction cancelDepositTx = (CancelDepositTransaction) tx;
                Transaction joinTx = ledgerService.getTx(cancelDepositTx.getTxData());

                PocJoinConsensusTransaction pocJoinTx = (PocJoinConsensusTransaction) joinTx;
                Consensus<Deposit> cDeposit = pocJoinTx.getTxData();

                Iterator<Consensus<Deposit>> it = depositList.iterator();
                while (it.hasNext()) {
                    Consensus<Deposit> tempDe = it.next();
                    if (tempDe.getHash().equals(cDeposit.getHash())) {
                        tempDe.setDelHeight(height);
                        break;
                    }
                }
            } else if (txType == TransactionConstant.TX_TYPE_STOP_AGENT) {

                StopAgentTransaction stopAgentTx = (StopAgentTransaction) tx;
                Transaction joinTx = ledgerService.getTx(stopAgentTx.getTxData());


                RegisterAgentTransaction registerAgentTx = (RegisterAgentTransaction) joinTx;

                Iterator<Consensus<Deposit>> it = depositList.iterator();
                while (it.hasNext()) {
                    Consensus<Deposit> tempDe = it.next();
                    Deposit deposit = tempDe.getExtend();
                    if (deposit.getAgentHash().equals(registerAgentTx.getTxData().getHexHash()) && tempDe.getDelHeight() == 0L) {
                        tempDe.setDelHeight(height);
                    }
                }

                Iterator<Consensus<Agent>> ita = agentList.iterator();
                while (ita.hasNext()) {
                    Consensus<Agent> tempCa = ita.next();
                    if (tempCa.getHash().equals(registerAgentTx.getTxData().getHash())) {
                        tempCa.setDelHeight(height);
                        break;
                    }
                }
            } else if (txType == TransactionConstant.TX_TYPE_YELLOW_PUNISH) {

                YellowPunishData yellowPunishData = ((YellowPunishTransaction) tx).getTxData();

                List<Address> addressList = yellowPunishData.getAddressList();

                long roundIndex = new BlockRoundData(block.getHeader().getExtend()).getRoundIndex();

                for (Address address : addressList) {
                    PunishLogPo punishLogPo = new PunishLogPo();
                    punishLogPo.setHeight(yellowPunishData.getHeight());
                    punishLogPo.setAddress(address.getBase58());
                    punishLogPo.setRoundIndex(roundIndex);
                    punishLogPo.setTime(tx.getTime());
                    punishLogPo.setType(PunishType.YELLOW.getCode());

                    yellowList.add(punishLogPo);
                }

            } else if (txType == TransactionConstant.TX_TYPE_RED_PUNISH) {

                RedPunishData redPunishData = ((RedPunishTransaction) tx).getTxData();

                String address = redPunishData.getAddress();

                long roundIndex = new BlockRoundData(block.getHeader().getExtend()).getRoundIndex();

                PunishLogPo punishLogPo = new PunishLogPo();
                punishLogPo.setHeight(redPunishData.getHeight());
                punishLogPo.setAddress(address);
                punishLogPo.setRoundIndex(roundIndex);
                punishLogPo.setTime(tx.getTime());
                punishLogPo.setType(PunishType.RED.getCode());

                redList.add(punishLogPo);
            }
        }

        chain.setEndBlockHeader(block.getHeader());
        blockList.add(block);
        blockHeaderList.add(block.getHeader());

        return true;
    }

    public boolean verifyBlock(Block block) {
        return verifyBlock(block, false);
    }


    public boolean verifyBlock(Block block, boolean isDownload) {

        if (block == null || chain.getEndBlockHeader() == null) {
            return false;
        }

        BlockHeader blockHeader = block.getHeader();

        if (blockHeader == null) {
            return false;
        }

        block.verifyWithException();

        // Verify that the block is properly connected
        // 验证区块是否正确连接
        String preHash = blockHeader.getPreHash().getDigestHex();

        if (!preHash.equals(chain.getEndBlockHeader().getHash().getDigestHex())) {
            BlockLog.debug("block height " + blockHeader.getHeight() + " prehash is error! hash :" + blockHeader.getHash().getDigestHex());
//            Log.error("block height " + blockHeader.getHeight() + " prehash is error! hash :" + blockHeader.getHash().getDigestHex());
            return false;
        }

        BlockRoundData roundData = new BlockRoundData(blockHeader.getExtend());

        MeetingRound currentRound = roundManager.getCurrentRound();

        if (isDownload && currentRound.getIndex() > roundData.getRoundIndex()) {

            MeetingRound round = roundManager.getRoundByIndex(roundData.getRoundIndex());
            if (round != null) {
                currentRound = round;
            }
        }

        boolean hasChangeRound = false;

        // Verify that the block round and time are correct
        // 验证区块轮次和时间是否正确
//        if(roundData.getRoundIndex() > currentRound.getIndex()) {
//            Log.error("block height " + blockHeader.getHeight() + " round index is error!");
//            return false;
//        }
        if (roundData.getRoundIndex() > currentRound.getIndex()) {
            if (roundData.getRoundStartTime() > TimeService.currentTimeMillis()) {
                BlockLog.debug("block height " + blockHeader.getHeight() + " round startTime is error, greater than current time! hash :" + blockHeader.getHash().getDigestHex());
//                Log.error("block height " + blockHeader.getHeight() + " round startTime is error, greater than current time! hash :" + blockHeader.getHash().getDigestHex());
                return false;
            }
            if (!isDownload && (roundData.getRoundStartTime() + (roundData.getPackingIndexOfRound() - 1) * ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L) > TimeService.currentTimeMillis()) {
                BlockLog.debug("block height " + blockHeader.getHeight() + " is the block of the future and received in advance! hash :" + blockHeader.getHash().getDigestHex());
//                Log.error("block height " + blockHeader.getHeight() + " is the block of the future and received in advance! hash :" + blockHeader.getHash().getDigestHex());
                return false;
            }
            MeetingRound tempRound = roundManager.getNextRound(roundData, !isDownload);
            tempRound.setPreRound(currentRound);
            currentRound = tempRound;
            hasChangeRound = true;
        } else if (roundData.getRoundIndex() < currentRound.getIndex()) {
            MeetingRound preRound = currentRound.getPreRound();
            while (preRound != null) {
                if (roundData.getRoundIndex() == preRound.getIndex()) {
                    currentRound = preRound;
                    break;
                }
                preRound = preRound.getPreRound();
            }
        }

        if (roundData.getRoundIndex() != currentRound.getIndex() || roundData.getRoundStartTime() != currentRound.getStartTime()) {
            BlockLog.debug("block height " + blockHeader.getHeight() + " round startTime is error! hash :" + blockHeader.getHash().getDigestHex());
//            Log.error("block height " + blockHeader.getHeight() + " round startTime is error! hash :" + blockHeader.getHash().getDigestHex());
            return false;
        }

        Log.debug(currentRound.toString());

        if (roundData.getConsensusMemberCount() != currentRound.getMemberCount()) {
            BlockLog.debug("block height " + blockHeader.getHeight() + " packager count is error! hash :" + blockHeader.getHash().getDigestHex());
//            Log.error("block height " + blockHeader.getHeight() + " packager count is error! hash :" + blockHeader.getHash().getDigestHex());
            return false;
        }
        // Verify that the packager is correct
        // 验证打包人是否正确
        MeetingMember member = currentRound.getMember(roundData.getPackingIndexOfRound());
        String packager = Address.fromHashs(blockHeader.getPackingAddress()).getBase58();
        if (!member.getPackingAddress().equals(packager)) {
            BlockLog.debug("block height " + blockHeader.getHeight() + " time error! hash :" + blockHeader.getHash().getDigestHex());
//            Log.error("block height " + blockHeader.getHeight() + " time error! hash :" + blockHeader.getHash().getDigestHex());
            return false;
        }

        if (member.getPackEndTime() != block.getHeader().getTime()) {
            BlockLog.debug("block height " + blockHeader.getHeight() + " time error! hash :" + blockHeader.getHash().getDigestHex());
//            Log.error("block height " + blockHeader.getHeight() + " time error! hash :" + blockHeader.getHash().getDigestHex());
            return false;
        }

        boolean success = verifyBaseTx(block, currentRound, member);
        if (!success) {
            BlockLog.debug("block height " + blockHeader.getHeight() + " verify tx error! hash :" + blockHeader.getHash().getDigestHex());
//            Log.error("block height " + blockHeader.getHeight() + " verify tx error! hash :" + blockHeader.getHash().getDigestHex());
            return false;
        }

        if (hasChangeRound) {
            roundManager.addRound(currentRound);
        }
        return true;
    }

    // Verify conbase transactions and penalties
    // 验证conbase交易和处罚交易
    private boolean verifyBaseTx(Block block, MeetingRound currentRound, MeetingMember member) {
        if(5176 == block.getHeader().getHeight()) {
            System.out.println("in the bug");
        }
        List<Transaction> txs = block.getTxs();
        Transaction tx = txs.get(0);
        if (tx.getType() != TransactionConstant.TX_TYPE_COIN_BASE) {
            BlockLog.debug("Coinbase transaction order wrong! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
//            Log.error("Coinbase transaction order wrong! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
            return false;
        }
        YellowPunishTransaction yellowPunishTx = null;
        for (int i = 1; i < txs.size(); i++) {
            Transaction transaction = txs.get(i);
            if (transaction.getType() == TransactionConstant.TX_TYPE_COIN_BASE) {
                BlockLog.debug("Coinbase transaction more than one! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
//                Log.error("Coinbase transaction more than one! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
                return false;
            }
            if (null == yellowPunishTx && transaction.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH) {
                yellowPunishTx = (YellowPunishTransaction) transaction;
            } else if (null != yellowPunishTx && transaction.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH) {
                BlockLog.debug("Yellow punish transaction more than one! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
//                Log.error("Yellow punish transaction more than one! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
                return false;
            }
        }

        CoinBaseTransaction coinBaseTransaction = ConsensusTool.createCoinBaseTx(member, block.getTxs(), currentRound, block.getHeader().getHeight() + PocConsensusConstant.COINBASE_UNLOCK_HEIGHT);
        if (null == coinBaseTransaction || !tx.getHash().equals(coinBaseTransaction.getHash())) {
            BlockLog.debug("the coin base tx is wrong! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
            Log.error("the coin base tx is wrong! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
            Log.error("Pierre-error-test: tx is <" + tx.toString() + ">, coinBaseTransaction is <" + coinBaseTransaction.toString() + ">");
            return false;
        }

        try {
            YellowPunishTransaction yellowPunishTransaction = ConsensusTool.createYellowPunishTx(chain.getBestBlock(), member, currentRound);
            if (yellowPunishTransaction == yellowPunishTx) {
                return true;
            } else if (yellowPunishTransaction == null || yellowPunishTx == null) {
                BlockLog.debug("The yellow punish tx is wrong! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
//                Log.error("The yellow punish tx is wrong! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
                return false;
            } else if (!yellowPunishTransaction.getHash().equals(yellowPunishTx.getHash())) {
                BlockLog.debug("The yellow punish tx's hash is wrong! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
//                Log.error("The yellow punish tx's hash is wrong! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex());
                return false;
            }

        } catch (Exception e) {
            BlockLog.debug("The tx's wrong! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex(), e);
//            Log.error("The tx's wrong! height: " + block.getHeader().getHeight() + " , hash : " + block.getHeader().getHash().getDigestHex(), e);
            return false;
        }

        return true;
    }

    public boolean verifyAndAddBlock(Block block, boolean isDownload) {
        boolean success = verifyBlock(block, isDownload);
        if (success) {
            success = addBlock(block);
        }
        return success;
    }

    public boolean rollback(Block block) {

        Block bestBlock = chain.getBestBlock();

        if (block == null || !block.getHeader().getHash().equals(bestBlock.getHeader().getHash())) {
            Log.warn("rollback block is not best block");
            return false;
        }

        List<Block> blockList = chain.getBlockList();

        if (blockList == null || blockList.size() == 0) {
            return false;
        }
        if (blockList.size() <= 2) {
            addBlockInBlockList(blockList);
        }

        blockList.remove(blockList.size() - 1);

        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();

        chain.setEndBlockHeader(blockHeaderList.get(blockHeaderList.size() - 2));
        BlockHeader rollbackBlockHeader = blockHeaderList.remove(blockHeaderList.size() - 1);

        // update txs
        List<Consensus<Agent>> agentList = chain.getAgentList();
        List<Consensus<Deposit>> depositList = chain.getDepositList();
        List<PunishLogPo> yellowList = chain.getYellowPunishList();
        List<PunishLogPo> redPunishList = chain.getRedPunishList();

        long height = rollbackBlockHeader.getHeight();

        for (int i = agentList.size() - 1; i >= 0; i--) {
            Consensus<Agent> agentConsensus = agentList.get(i);
            Agent agent = agentConsensus.getExtend();

            if (agentConsensus.getDelHeight() == height) {
                agentConsensus.setDelHeight(0L);
            }

            if (agent.getBlockHeight() == height) {
                agentList.remove(i);
            }
        }

        for (int i = depositList.size() - 1; i >= 0; i--) {
            Consensus<Deposit> tempDe = depositList.get(i);
            Deposit deposit = tempDe.getExtend();

            if (tempDe.getDelHeight() == height) {
                tempDe.setDelHeight(0L);
            }

            if (deposit.getBlockHeight() == height) {
                depositList.remove(i);
            }
        }

        for (int i = yellowList.size() - 1; i >= 0; i--) {
            PunishLogPo tempYellow = yellowList.get(i);
            if (tempYellow.getHeight() < height) {
                break;
            }
            if (tempYellow.getHeight() == height) {
                yellowList.remove(i);
            }
        }

        for (int i = redPunishList.size() - 1; i >= 0; i--) {
            PunishLogPo redPunish = redPunishList.get(i);
            if (redPunish.getHeight() < height) {
                break;
            }
            if (redPunish.getHeight() == height) {
                redPunishList.remove(i);
            }
        }

        // 判断是否需要重新计算轮次
        roundManager.checkIsNeedReset();

        return true;
    }

    private void addBlockInBlockList(List<Block> blockList) {
        String firstHash = blockList.get(0).getHeader().getPreHash().getDigestHex();
        Block block = blockService.getBlock(firstHash);
        blockList.add(0, block);
    }

    /**
     * Get the state of the complete chain after the combination of a chain and the current chain bifurcation point, that is, first obtain the bifurcation point between the bifurcation chain and the current chain.
     * Then create a brand new chain, copy all the states before the bifurcation point of the main chain to the brand new chain
     * <p>
     * 获取一条链与当前链分叉点组合之后的完整链的状态，也就是，先获取到分叉链与当前链的分叉点，
     * 然后创建一条全新的链，把主链分叉点之前的所有状态复制到全新的链
     *
     * @return ChainContainer
     */
    public ChainContainer getBeforeTheForkChain(ChainContainer chainContainer) {

        Chain newChain = new Chain();
        newChain.setId(chainContainer.getChain().getId());
        newChain.setStartBlockHeader(chain.getStartBlockHeader());
        newChain.setEndBlockHeader(chain.getEndBlockHeader());
        newChain.setBlockHeaderList(new ArrayList<>(chain.getBlockHeaderList()));
        newChain.setBlockList(new ArrayList<>(chain.getBlockList()));

        if (chain.getAgentList() != null) {
            List<Consensus<Agent>> agentList = new ArrayList<>();

            for(Consensus<Agent> agentConsensus : chain.getAgentList()) {
                agentList.add(ConsensusTool.copyConsensusAgent(agentConsensus));
            }

            newChain.setAgentList(agentList);
        }
        if (chain.getDepositList() != null) {
            List<Consensus<Deposit>> depositList = new ArrayList<>();

            for(Consensus<Deposit> depositConsensus : chain.getDepositList()) {
                depositList.add(ConsensusTool.copyConsensusDeposit(depositConsensus));
            }

            newChain.setDepositList(depositList);
        }
        if (chain.getYellowPunishList() != null) {
            newChain.setYellowPunishList(new ArrayList<>(chain.getYellowPunishList()));
        }
        if (chain.getRedPunishList() != null) {
            newChain.setRedPunishList(new ArrayList<>(chain.getRedPunishList()));
        }
        ChainContainer newChainContainer = new ChainContainer(newChain);

        // Bifurcation
        // 分叉点
        BlockHeader pointBlockHeader = chainContainer.getChain().getStartBlockHeader();

        List<Block> blockList = newChain.getBlockList();
        for (int i = blockList.size() - 1; i >= 0; i--) {
            Block block = blockList.get(i);
            if (pointBlockHeader.getPreHash().equals(block.getHeader().getHash())) {
                break;
            }
            newChainContainer.rollback(block);
        }

        newChainContainer.initRound();

        return newChainContainer;
    }

    /**
     * Get the block information of the current chain and branch chain after the cross point and combine them into a new branch chain
     * <p>
     * 获取当前链与分叉链对比分叉点之后的区块信息，组合成一个新的分叉链
     *
     * @return ChainContainer
     */
    public ChainContainer getAfterTheForkChain(ChainContainer chainContainer) {

        // Bifurcation
        // 分叉点
        BlockHeader pointBlockHeader = chainContainer.getChain().getStartBlockHeader();

        Chain chain = new Chain();

        List<Block> blockList = getChain().getBlockList();

        boolean canAdd = false;
        for (int i = 0; i < blockList.size(); i++) {

            Block block = blockList.get(i);

            if (canAdd) {
                chain.getBlockList().add(block);
                chain.getBlockHeaderList().add(block.getHeader());
            }

            if (pointBlockHeader.getPreHash().equals(block.getHeader().getHash())) {
                canAdd = true;
                if (i + 1 < blockList.size()) {
                    chain.setStartBlockHeader(blockList.get(i + 1).getHeader());
                    chain.setEndBlockHeader(getChain().getEndBlockHeader());
                    chain.setPreChainId(chainContainer.getChain().getId());
                }
                continue;
            }
        }
        return new ChainContainer(chain);
    }

    public MeetingRound getCurrentRound() {
        return roundManager.getCurrentRound();
    }

    public MeetingRound getOrResetCurrentRound() {
        return roundManager.resetRound(true);
    }

    public MeetingRound initRound() {
        return roundManager.initRound();
    }

    public void clearRound(int count) {
        roundManager.clearRound(count);
    }

    public Block getBestBlock() {
        return chain.getBestBlock();
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ChainContainer)) {
            return false;
        }
        ChainContainer other = (ChainContainer) obj;
        if (other.getChain() == null || this.chain == null) {
            return false;
        }
        return other.getChain().getId().equals(this.chain.getId());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

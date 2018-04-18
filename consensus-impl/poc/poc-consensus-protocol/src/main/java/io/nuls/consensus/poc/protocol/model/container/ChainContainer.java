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

package io.nuls.consensus.poc.protocol.model.container;

import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.poc.protocol.constant.ConsensusStatusEnum;
import io.nuls.consensus.poc.protocol.constant.PocConsensusConstant;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.context.ConsensusContext;
import io.nuls.consensus.poc.protocol.locker.Lockers;
import io.nuls.consensus.poc.protocol.model.*;
import io.nuls.consensus.poc.protocol.model.block.BlockRoundData;
import io.nuls.consensus.poc.protocol.tx.*;
import io.nuls.consensus.poc.protocol.tx.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.entity.YellowPunishData;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.ConsensusLog;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class ChainContainer implements Cloneable {

    private Chain chain;

    private List<MeetingRound> roundList = new ArrayList<>();

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);

    public ChainContainer() {
    }

    public ChainContainer(Chain chain) {
        this.chain = chain;
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

        long height = block.getHeader().getHeight();

        List<Transaction> txs = block.getTxs();
        for (Transaction tx : txs) {
            int txType = tx.getType();
            if (txType == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
                // Registered agent transaction
                // 注册代理交易
                RegisterAgentTransaction registerAgentTx = (RegisterAgentTransaction) tx;
                Consensus<Agent> ca = registerAgentTx.getTxData();
                agentList.add(ca);
            } else if (txType == TransactionConstant.TX_TYPE_JOIN_CONSENSUS) {
                PocJoinConsensusTransaction joinConsensusTx = (PocJoinConsensusTransaction) tx;
                Consensus<Deposit> cDeposit = joinConsensusTx.getTxData();
                depositList.add(cDeposit);
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
                    if (deposit.getAgentHash().equals(registerAgentTx.getHash())) {
                        tempDe.setDelHeight(height);
                    }
                }

                Iterator<Consensus<Agent>> ita = agentList.iterator();
                while (ita.hasNext()) {
                    Consensus<Agent> tempCa = ita.next();
                    if (tempCa.getHash().equals(registerAgentTx.getHash())) {
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

                yellowList.add(punishLogPo);
            }
        }

        chain.setEndBlockHeader(block.getHeader());
        blockList.add(block);
        blockHeaderList.add(block.getHeader());

        //TODO 是否需要重新计算轮次 ？

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

        // Verify that the block is properly connected
        // 验证区块是否正确连接
        String preHash = blockHeader.getPreHash().getDigestHex();

        if (!preHash.equals(chain.getEndBlockHeader().getHash().getDigestHex())) {
            return false;
        }

        BlockRoundData roundData = new BlockRoundData(blockHeader.getExtend());

        MeetingRound currentRound = getCurrentRound();

        if (isDownload && currentRound.getIndex() > roundData.getRoundIndex()) {
            for (int i = roundList.size() - 1; i >= 0; i--) {
                currentRound = roundList.get(i);
                if (currentRound.getIndex() == roundData.getRoundIndex()) {
                    break;
                }
            }
        }

        if (currentRound == null) {
            currentRound = resetRound(!isDownload);
        }

        boolean hasChangeRound = false;

        // Verify that the block round and time are correct
        // 验证区块轮次和时间是否正确
//        if(roundData.getRoundIndex() > currentRound.getIndex()) {
//            Log.error("block height " + blockHeader.getHeight() + " round index is error!");
//            return false;
//        }

        if (roundData.getRoundIndex() == currentRound.getIndex() && roundData.getRoundStartTime() != currentRound.getStartTime()) {
            Log.error("block height " + blockHeader.getHeight() + " round startTime is error!");
            return false;
        }
        if (roundData.getRoundIndex() > currentRound.getIndex()) {
            if (roundData.getRoundStartTime() > TimeService.currentTimeMillis()) {
                Log.error("block height " + blockHeader.getHeight() + " round startTime is error, greater than current time!");
                return false;
            }
            if (!isDownload && (roundData.getRoundStartTime() + roundData.getPackingIndexOfRound() * ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L) > TimeService.currentTimeMillis()) {
                Log.error("block height " + blockHeader.getHeight() + " is the block of the future and received in advance!");
                return false;
            }
            currentRound = getNextRound(roundData, !isDownload);
            hasChangeRound = true;
        }

        Log.debug(currentRound.toString());

        if (roundData.getConsensusMemberCount() != currentRound.getMemberCount()) {
            Log.error("block height " + blockHeader.getHeight() + " packager count is error!");
            return false;
        }
        // Verify that the packager is correct
        // 验证打包人是否正确
        MeetingMember member = currentRound.getMember(roundData.getPackingIndexOfRound());
        String packager = Address.fromHashs(blockHeader.getPackingAddress()).getBase58();
        if (!member.getPackingAddress().equals(packager)) {
            Log.error("block height " + blockHeader.getHeight() + " packager is error!");
            return false;
        }

        boolean success = verifyBaseTx(block, currentRound, member);
        if (!success) {
            Log.error("block height " + blockHeader.getHeight() + " verify tx error!");
            return false;
        }

        if (hasChangeRound) {
            roundList.add(currentRound);
        }
        return true;
    }

    // Verify conbase transactions and penalties
    // 验证conbase交易和处罚交易
    private boolean verifyBaseTx(Block block, MeetingRound currentRound, MeetingMember member) {
        List<Transaction> txs = block.getTxs();
        Transaction tx = txs.get(0);
        if (tx.getType() != TransactionConstant.TX_TYPE_COIN_BASE) {
            Log.error("Coinbase transaction order wrong!");
            return false;
        }
        YellowPunishTransaction yellowPunishTx = null;
        for (int i = 1; i < txs.size(); i++) {
            Transaction transaction = txs.get(i);
            if (transaction.getType() == TransactionConstant.TX_TYPE_COIN_BASE) {
                Log.error("Coinbase transaction more than one!");
                return false;
            }
            if (null == yellowPunishTx && transaction.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH) {
                yellowPunishTx = (YellowPunishTransaction) transaction;
            } else if (null != yellowPunishTx && transaction.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH) {
                Log.error("Yellow punish transaction more than one!");
                return false;
            }
        }

        CoinBaseTransaction coinBaseTransaction = ConsensusTool.createCoinBaseTx(member, block.getTxs(), currentRound);
        if (null == coinBaseTransaction || !tx.getHash().equals(coinBaseTransaction.getHash())) {
            Log.error("the coin base tx is wrong!");
            return false;
        }

        try {
            YellowPunishTransaction yellowPunishTransaction = ConsensusTool.createYellowPunishTx(chain.getBestBlock(), member, currentRound);
            if (yellowPunishTransaction == yellowPunishTx) {
                return true;
            } else if (yellowPunishTransaction == null || yellowPunishTx == null) {
                Log.error("The yellow punish tx is wrong!");
                return false;
            } else if (!yellowPunishTransaction.getHash().equals(yellowPunishTx.getHash())) {
                Log.error("The yellow punish tx's hash is wrong!");
                return false;
            }

        } catch (Exception e) {
            Log.error(e);
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

    public boolean rollback() {

        List<Block> blockList = chain.getBlockList();

        if (blockList == null || blockList.size() == 0) {
            return false;
        }

        Block rollbackBlock = blockList.get(blockList.size() - 1);
        blockList.remove(rollbackBlock);

        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        chain.setEndBlockHeader(blockHeaderList.get(blockHeaderList.size() - 2));
        BlockHeader rollbackBlockHeader = blockHeaderList.get(blockHeaderList.size() - 1);
        blockHeaderList.remove(rollbackBlockHeader);

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
                agentConsensus.setDelHeight(0);
            }

            if (agent.getBlockHeight() == height) {
                depositList.remove(i);
            }
        }

        for (int i = depositList.size() - 1; i >= 0; i--) {
            Consensus<Deposit> tempDe = depositList.get(i);
            Deposit deposit = tempDe.getExtend();

            if (tempDe.getDelHeight() == height) {
                tempDe.setDelHeight(0);
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

        //TODO 是否需要重新计算轮次 ？

        return true;
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
        newChain.setBlockHeaderList(new ArrayList(chain.getBlockHeaderList()));
        newChain.setBlockList(new ArrayList<>(chain.getBlockList()));

        if(chain.getAgentList() != null) {
            newChain.setAgentList(new ArrayList<>(chain.getAgentList()));
        }
        if(chain.getDepositList() != null) {
            newChain.setDepositList(new ArrayList<>(chain.getDepositList()));
        }
        if(chain.getYellowPunishList() != null) {
            newChain.setYellowPunishList(new ArrayList<>(chain.getYellowPunishList()));
        }
        if(chain.getRedPunishList() != null) {
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
            newChainContainer.rollback();
        }

        newChainContainer.getRoundList().clear();
        newChainContainer.resetRound(false);

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
        List<BlockHeader> blockHeaderList = getChain().getBlockHeaderList();

        boolean canAdd = false;
        for (int i = 0; i < blockList.size(); i++) {

            BlockHeader blockHeader = blockHeaderList.get(i);

            if (canAdd) {
                Block block = blockList.get(i);
                chain.getBlockList().add(block);
                chain.getBlockHeaderList().add(blockHeader);
            }

            if (pointBlockHeader.getPreHash().equals(blockHeader.getHash())) {
                canAdd = true;
                if (i + 1 < blockHeaderList.size()) {
                    chain.setStartBlockHeader(blockHeaderList.get(i + 1));
                    chain.setEndBlockHeader(getChain().getEndBlockHeader());
                    chain.setPreChainId(chainContainer.getChain().getId());
                }
                continue;
            }
        }
        return new ChainContainer(chain);
    }

    public MeetingRound getCurrentRound() {
        Lockers.ROUND_LOCK.lock();
        try {
            if (roundList == null || roundList.size() == 0) {
                return null;
            }
            MeetingRound round = roundList.get(roundList.size() - 1);
            if (round.getPreRound() == null && roundList.size() >= 2) {
                round.setPreRound(roundList.get(roundList.size() - 2));
            }
            return round;
        } finally {
            Lockers.ROUND_LOCK.unlock();
        }
    }

    public MeetingRound resetRound(boolean isRealTime) {

        MeetingRound round = getCurrentRound();

        if (isRealTime) {
            if (round == null || round.getEndTime() < TimeService.currentTimeMillis()) {
                round = getNextRound(null, true);
                Lockers.ROUND_LOCK.lock();
                try {
                    roundList.add(round);
                } finally {
                    Lockers.ROUND_LOCK.unlock();
                }
            }
            return round;
        }

        BlockRoundData roundData = new BlockRoundData(chain.getEndBlockHeader().getExtend());

        if (round != null && roundData.getRoundIndex() == round.getIndex() && roundData.getPackingIndexOfRound() != roundData.getConsensusMemberCount()) {
            return round;
        }

        round = getNextRound(null, false);
        Lockers.ROUND_LOCK.lock();
        try {
            roundList.add(round);
        } finally {
            Lockers.ROUND_LOCK.unlock();
        }
        return round;
    }

    public MeetingRound getNextRound(BlockRoundData roundData, boolean isRealTime) {
        Lockers.ROUND_LOCK.lock();
        try {
            if (isRealTime && roundData == null) {
                return getNextRoudByRealTime();
            } else if (!isRealTime && roundData == null) {
                return getNextRoundByNotRealTime();
            } else {
                return getNextRoundByExpectedRound(roundData);
            }
        } finally {
            Lockers.ROUND_LOCK.unlock();
        }
    }

    private MeetingRound getNextRoudByRealTime() {

        BlockHeader bestBlockHeader = chain.getEndBlockHeader();

        BlockHeader startBlockHeader = bestBlockHeader;

        BlockRoundData bestRoundData = new BlockRoundData(bestBlockHeader.getExtend());

        if (startBlockHeader.getHeight() != 0l) {
            long roundIndex = bestRoundData.getRoundIndex();
            if (bestRoundData.getConsensusMemberCount() == bestRoundData.getPackingIndexOfRound() || TimeService.currentTimeMillis() >= bestRoundData.getRoundEndTime()) {
                roundIndex += 1;
            }
            startBlockHeader = getFirstBlockHeightOfPreRoundByRoundIndex(roundIndex);
        }

        long nowTime = TimeService.currentTimeMillis();
        long index = 0l;
        long startTime = 0l;

        if (nowTime < bestRoundData.getRoundEndTime()) {
            index = bestRoundData.getRoundIndex();
            startTime = bestRoundData.getRoundStartTime();
        } else {
            long diffTime = nowTime - bestRoundData.getRoundEndTime();
            int diffRoundCount = (int) (diffTime / (bestRoundData.getConsensusMemberCount() * ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L));
            index = bestRoundData.getRoundIndex() + diffRoundCount + 1;
            startTime = bestRoundData.getRoundEndTime() + diffRoundCount * bestRoundData.getConsensusMemberCount() * ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L;
        }
        return calculationRound(startBlockHeader, index, startTime);
    }

    private MeetingRound getNextRoundByNotRealTime() {
        BlockHeader bestBlockHeader = chain.getEndBlockHeader();
        BlockRoundData roundData = new BlockRoundData(bestBlockHeader.getExtend());
        return getNextRoundByExpectedRound(roundData);
    }

    private MeetingRound getNextRoundByExpectedRound(BlockRoundData roundData) {
        BlockHeader startBlockHeader = chain.getEndBlockHeader();

        long roundIndex = roundData.getRoundIndex();
        long roundStartTime = roundData.getRoundStartTime();
        if (startBlockHeader.getHeight() != 0l) {
//            if(roundData.getConsensusMemberCount() == roundData.getPackingIndexOfRound()) {
//                roundIndex += 1;
//                roundStartTime = roundData.getRoundEndTime();
//            }
            startBlockHeader = getFirstBlockHeightOfPreRoundByRoundIndex(roundIndex);
        }

        return calculationRound(startBlockHeader, roundIndex, roundStartTime);
    }

    private MeetingRound calculationRound(BlockHeader startBlockHeader, long index, long startTime) {

        MeetingRound round = new MeetingRound();

        round.setIndex(index);
        round.setStartTime(startTime);

        setMemberList(round, startBlockHeader);

        round.calcLocalPacker(accountService.getAccountList());

        ConsensusLog.debug("calculation||index:{},startTime:{},startHeight:{},hash:{}\n" + round.toString(), index, startTime, startBlockHeader.getHeight(), startBlockHeader.getHash());
        return round;
    }

    private void setMemberList(MeetingRound round, BlockHeader startBlockHeader) {

        List<MeetingMember> memberList = new ArrayList<>();
        double totalWeight = 0;
        for (String address : ConsensusContext.getSeedNodeList()) {
            MeetingMember member = new MeetingMember();
            member.setAgentAddress(address);
            member.setPackingAddress(address);
            member.setCreditVal(1);
            member.setRoundStartTime(round.getStartTime());
            memberList.add(member);
        }
        List<Consensus<Agent>> agentList = getAliveAgentList(startBlockHeader.getHeight());
        for (Consensus<Agent> ca : agentList) {
            MeetingMember member = new MeetingMember();
            member.setAgentConsensus(ca);
            member.setAgentHash(ca.getHexHash());
            member.setAgentAddress(ca.getAddress());
            member.setPackingAddress(ca.getExtend().getPackingAddress());
            member.setOwnDeposit(ca.getExtend().getDeposit());
            member.setCommissionRate(ca.getExtend().getCommissionRate());
            member.setRoundStartTime(round.getStartTime());

            List<Consensus<Deposit>> cdlist = getDepositListByAgentId(ca.getHexHash(), startBlockHeader.getHeight());
            for (Consensus<Deposit> cd : cdlist) {
                member.setTotalDeposit(member.getTotalDeposit().add(cd.getExtend().getDeposit()));
            }
            member.setDepositList(cdlist);
            member.setCreditVal(calcCreditVal(member, startBlockHeader));
            ca.getExtend().setCreditVal(member.getRealCreditVal());
            ca.getExtend().setTotalDeposit(member.getTotalDeposit().getValue());
            boolean isItIn = member.getTotalDeposit().isGreaterOrEquals(PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT);
            if (isItIn) {
                ca.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
                totalWeight = DoubleUtils.sum(totalWeight, DoubleUtils.mul(ca.getExtend().getDeposit().getValue(), member.getCalcCreditVal()));
                totalWeight = DoubleUtils.sum(totalWeight, DoubleUtils.mul(member.getTotalDeposit().getValue(), member.getCalcCreditVal()));
                memberList.add(member);
            } else {
                ca.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
            }
        }

        Collections.sort(memberList);

        for (int i = 0; i < memberList.size(); i++) {
            MeetingMember member = memberList.get(i);
            member.setPackingIndexOfRound(i + 1);
        }

        round.setMemberCount(memberList.size());
        round.setMemberList(memberList);
        round.setTotalWeight(totalWeight);
    }

    private List<Consensus<Deposit>> getDepositListByAgentId(String agentId, long startBlockHeight) {

        List<Consensus<Deposit>> depositList = chain.getDepositList();
        List<Consensus<Deposit>> resultList = new ArrayList<>();

        for (int i = depositList.size() - 1; i >= 0; i--) {
            Consensus<Deposit> cd = depositList.get(i);
            if (cd.getDelHeight() != 0 && cd.getDelHeight() <= startBlockHeight) {
                continue;
            }
            Deposit deposit = cd.getExtend();
            if (deposit.getBlockHeight() >= startBlockHeight || deposit.getBlockHeight() < 0) {
                continue;
            }
            if (!deposit.getAgentHash().equals(agentId)) {
                continue;
            }
            resultList.add(cd);
        }

        return resultList;
    }

    private List<Consensus<Agent>> getAliveAgentList(long startBlockHeight) {
        List<Consensus<Agent>> resultList = new ArrayList<>();
        for (int i = chain.getAgentList().size() - 1; i >= 0; i--) {
            Consensus<Agent> ca = chain.getAgentList().get(i);
            if (ca.getDelHeight() != 0 && ca.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (ca.getExtend().getBlockHeight() >= startBlockHeight || ca.getExtend().getBlockHeight() < 0) {
                continue;
            }
            resultList.add(ca);
        }
        return resultList;
    }

    private double calcCreditVal(MeetingMember member, BlockHeader blockHeader) {

        BlockRoundData roundData = new BlockRoundData(blockHeader.getExtend());

        long roundStart = roundData.getRoundIndex() - PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        long blockCount = getBlockCountByAddress(member.getPackingAddress(), roundStart, roundData.getRoundIndex());
        long sumRoundVal = getPunishCountByAddress(member.getAgentAddress(), roundStart, roundData.getRoundIndex(), PunishType.YELLOW.getCode());
        double ability = DoubleUtils.div(blockCount, PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);

        double penalty = DoubleUtils.div(DoubleUtils.mul(PocConsensusConstant.CREDIT_MAGIC_NUM, sumRoundVal),
                DoubleUtils.mul(PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT, PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT));

//        BlockLog.debug(")))))))))))))creditVal:" + DoubleUtils.sub(ability, penalty) + ",member:" + member.getAgentAddress());
//        BlockLog.debug(")))))))))))))blockCount:" + blockCount + ", start:" + roundStart + ",end:" + calcRoundIndex + ", yellowCount:" + sumRoundVal);

        return ability - penalty;
    }

    private long getPunishCountByAddress(String address, long roundStart, long roundEnd, int code) {
        long count = 0;
        List<PunishLogPo> punishList = chain.getYellowPunishList();

        if (code == PunishType.RED.getCode()) {
            punishList = chain.getRedPunishList();
        }

        for (int i = punishList.size() - 1; i >= 0; i--) {
            PunishLogPo punish = punishList.get(i);

            if (punish.getRoundIndex() > roundEnd) {
                continue;
            }
            if (punish.getRoundIndex() < roundStart) {
                break;
            }

            if (punish.getAddress().equals(address)) {
                count++;
            }
        }
        return count;
    }

    private long getBlockCountByAddress(String packingAddress, long roundStart, long roundEnd) {
        long count = 0;
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();

        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            BlockRoundData roundData = new BlockRoundData(blockHeader.getExtend());

            if (roundData.getRoundIndex() > roundEnd) {
                continue;
            }
            if (roundData.getRoundIndex() < roundStart) {
                break;
            }

            if (Address.fromHashs(blockHeader.getPackingAddress()).getBase58().equals(packingAddress)) {
                count++;
            }
        }
        return count;
    }

    private BlockHeader getFirstBlockHeightOfPreRoundByRoundIndex(long roundIndex) {
        BlockHeader firstBlockHeader = null;
        long startRoundIndex = 0l;
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            long currentRoundIndex = new BlockRoundData(blockHeader.getExtend()).getRoundIndex();
            if (roundIndex > currentRoundIndex) {
                if (startRoundIndex == 0l) {
                    startRoundIndex = currentRoundIndex;
                }
                if (currentRoundIndex < startRoundIndex) {
                    firstBlockHeader = blockHeaderList.get(i + 1);
//                    firstBlockHeader = blockHeaderList.get(i);
                    break;
                }
            }
        }
        if (firstBlockHeader == null) {
            firstBlockHeader = chain.getEndBlockHeader();
            Log.warn("the first block of pre round not found");
        }
        return firstBlockHeader;
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public List<MeetingRound> getRoundList() {
        return roundList;
    }

    public void setRoundList(List<MeetingRound> roundList) {
        this.roundList = roundList;
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

    public Block getBestBlock() {
        return chain.getBestBlock();
    }
}

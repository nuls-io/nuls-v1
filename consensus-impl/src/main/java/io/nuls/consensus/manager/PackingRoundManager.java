/*
 *
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
 *
 */

package io.nuls.consensus.manager;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.ConsensusReward;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.service.impl.PocBlockService;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.params.OperationType;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;

import java.util.*;

/**
 * @author Niels
 * @date 2018/3/28
 */
public class PackingRoundManager {

    private static final PackingRoundManager VALIDATE_INSTANCE = new PackingRoundManager();
    private static final PackingRoundManager PACK_INSTANCE = new PackingRoundManager();

    private BlockService consensusBlockService;
    private PocBlockService pocBlockService = PocBlockService.getInstance();

    private ConsensusManager csManager = ConsensusManager.getInstance();
    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();

    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private List<PocMeetingMember> seedList;


    private PackingRoundManager() {
    }

    public static PackingRoundManager getValidateInstance() {
        return VALIDATE_INSTANCE;
    }

    public static PackingRoundManager getPackInstance() {
        return PACK_INSTANCE;
    }

    private PocMeetingRound currentRound;


    public ValidateResult validateBlockHeader(BlockHeader header) {
        return validate(header, null);
    }

    /**
     * @param block
     * @return
     */
    public ValidateResult validateBlock(Block block) {
        if (block.getHeader() == null || block.getTxs() == null || block.getTxs().isEmpty()) {
            return ValidateResult.getFailedResult("the block is not complete.");
        }
        return validate(block.getHeader(), block.getTxs());
    }

    public ValidateResult validate(BlockHeader header, List<Transaction> txs) {
        if (header.getHeight() == 0) {
            return ValidateResult.getSuccessResult();
        }
        BlockRoundData
                roundData = new BlockRoundData(header.getExtend());
        Block preBlock = getBlockService().getBlock(header.getPreHash().getDigestHex());
        if (null == preBlock) {
            //When a block does not exist, it is temporarily validated.
            return ValidateResult.getSuccessResult();
        }
        calc(preBlock);
        BlockRoundData preRoundData = new BlockRoundData(preBlock.getHeader().getExtend());

        PocMeetingRound localThisRoundData = this.currentRound;
        PocMeetingRound localPreRoundData;
        if (preRoundData.getRoundIndex() == roundData.getRoundIndex()) {
            localPreRoundData = localThisRoundData;
        } else {
            localPreRoundData = localThisRoundData.getPreRound();
            if (localPreRoundData == null) {
                localPreRoundData = calcCurrentRound(preBlock.getHeader(),preBlock.getHeader().getHeight(),preRoundData);
            }
        }

        if (roundData.getConsensusMemberCount() != localThisRoundData.getMemberCount()) {
            return ValidateResult.getFailedResult("The round data of the block is wrong!");
        }
        if (roundData.getRoundIndex() == (localPreRoundData.getIndex() + 1) && roundData.getRoundStartTime() != localPreRoundData.getEndTime()) {
            return ValidateResult.getFailedResult("The round data of the block is wrong!");
        }
        PocMeetingMember member = localThisRoundData.getMember(roundData.getPackingIndexOfRound());
        if (null == member) {
            return ValidateResult.getFailedResult("Cannot find the packing member!");
        }
        if (member.getIndexOfRound() != roundData.getPackingIndexOfRound() || !member.getPackingAddress().equals(header.getPackingAddress())) {
            ValidateResult vr = ValidateResult.getFailedResult("It's not the address's turn to pack the block!");
            vr.setObject(header);
            vr.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
            return vr;
        }
        if (null == txs) {
            return ValidateResult.getSuccessResult();
        }
        YellowPunishTransaction yellowPunishTx = null;
        for (Transaction tx : txs) {
            if (tx.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH) {
                if (yellowPunishTx == null) {
                    yellowPunishTx = (YellowPunishTransaction) tx;
                } else {
                    return ValidateResult.getFailedResult("There are too many yellow punish transactions!");
                }
            }
        }
        //when the blocks are continuous
        boolean isContinuous = preRoundData.getRoundIndex() == roundData.getRoundIndex() && preRoundData.getPackingIndexOfRound() == (roundData.getPackingIndexOfRound() - 1);
        isContinuous = isContinuous || (preRoundData.getRoundIndex() == (roundData.getRoundIndex() - 1) && preRoundData.getPackingIndexOfRound() == preRoundData.getConsensusMemberCount() &&
                roundData.getPackingIndexOfRound() == 1);
        //Too long intervals will not be penalized.
        boolean longTimeAgo = preRoundData.getRoundIndex() < (roundData.getRoundIndex() - 1);
        if (longTimeAgo && yellowPunishTx == null) {
            return ValidateResult.getSuccessResult();
        }
        if (isContinuous) {
            if (yellowPunishTx == null) {
                return ValidateResult.getSuccessResult();
            } else {
                return ValidateResult.getFailedResult("the block shouldn't has any yellow punish tx!");
            }
        } else {
            if (null == yellowPunishTx) {
                return ValidateResult.getFailedResult("It should be a yellow punish tx here!");
            }
            if (yellowPunishTx.getTxData().getHeight() != header.getHeight()) {
                return ValidateResult.getFailedResult("The yellow punish tx's height is wrong!");
            }
            int interval = 0;
            if (roundData.getRoundIndex() == preRoundData.getRoundIndex()) {
                interval = roundData.getPackingIndexOfRound() - preRoundData.getPackingIndexOfRound() - 1;
            } else if ((roundData.getRoundIndex() - 1) == preRoundData.getRoundIndex()) {
                interval = preRoundData.getConsensusMemberCount() - preRoundData.getPackingIndexOfRound() + roundData.getPackingIndexOfRound() - 1;
            }
            if (interval != yellowPunishTx.getTxData().getAddressList().size()) {
                return ValidateResult.getFailedResult("The count of YellowPunishTx is wrong,it should be " + interval);
            } else {
                long roundIndex = preRoundData.getRoundIndex();
                long indexOfRound = preRoundData.getPackingIndexOfRound() + 1;
                List<String> addressList = new ArrayList<>();
                while (true) {
                    PocMeetingRound round = getRoundData(roundIndex);
                    if (null == round) {
                        break;
                    }
                    if (roundIndex == roundData.getRoundIndex() && roundData.getPackingIndexOfRound() <= indexOfRound) {
                        break;
                    }
                    if (round.getMemberCount() < indexOfRound) {
                        roundIndex++;
                        indexOfRound = 1;
                        continue;
                    }
                    PocMeetingMember meetingMember = round.getMember(interval);
                    if (null == meetingMember) {
                        return ValidateResult.getFailedResult("the round data has error!");
                    }
                    addressList.add(meetingMember.getAgentAddress());
                    indexOfRound++;
                }
                if (addressList.size() != yellowPunishTx.getTxData().getAddressList().size()) {
                    return ValidateResult.getFailedResult("The block has wrong yellow punish Tx!address list size is wrong!");
                }
                for (String address : addressList) {
                    boolean contains = false;
                    for (Address addressObj : yellowPunishTx.getTxData().getAddressList()) {
                        if (addressObj.getBase58().equals(address)) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        return ValidateResult.getFailedResult("The block has wrong yellow punish Tx!It has wrong address");
                    }
                }
            }
        }

        return checkCoinBaseTx(header, txs, roundData, localThisRoundData);
    }

    /**
     * Verify that the time of the two turns is correct.
     *
     * @param localPreRoundData
     * @param localThisRoundData
     * @return
     */
    private ValidateResult checkThisRound(PocMeetingRound localPreRoundData, PocMeetingRound localThisRoundData, BlockRoundData thisBlockRoundData, BlockHeader header) {
        if (localPreRoundData.getEndTime() == localThisRoundData.getStartTime() && localPreRoundData.getIndex() == (localThisRoundData.getIndex() - 1)) {
            return ValidateResult.getSuccessResult();
        } else if (localPreRoundData.getIndex() == (localThisRoundData.getIndex() - 1)) {
            return ValidateResult.getFailedResult("There is no docking between the two rounds.");
        }
        long betweenTime = localThisRoundData.getStartTime() - localPreRoundData.getEndTime();

        long differenceOfRoundIndex = betweenTime / (localThisRoundData.getMemberCount() * 1000 * PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND);

        long differenceOfPackingIndex = betweenTime % (localThisRoundData.getMemberCount() * 1000 * PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND);

        if (!((localThisRoundData.getIndex() - localPreRoundData.getIndex()) == differenceOfRoundIndex && 0 == differenceOfPackingIndex)) {
            return ValidateResult.getFailedResult("There's no docking between the two rounds.");
        }
        long indexOfRound = 1 + (header.getTime() - thisBlockRoundData.getRoundStartTime()) / (1000 * PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND);
        if (thisBlockRoundData.getPackingIndexOfRound() == indexOfRound) {
            return ValidateResult.getSuccessResult();
        }
        return ValidateResult.getFailedResult("There's no docking between the two rounds.");
    }

    private ValidateResult checkCoinBaseTx(BlockHeader header, List<Transaction> txs, BlockRoundData roundData, PocMeetingRound localRound) {
        Transaction tx = txs.get(0);
        if (tx.getType() != TransactionConstant.TX_TYPE_COIN_BASE) {
            return ValidateResult.getFailedResult("Coinbase transaction order wrong!");
        }

        for (int i = 1; i < txs.size(); i++) {
            Transaction transaction = txs.get(i);
            if (transaction.getType() == TransactionConstant.TX_TYPE_COIN_BASE) {
                ValidateResult result = ValidateResult.getFailedResult("Coinbase transaction more than one!");
                result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
                return result;
            }
        }
        if (null == localRound) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "local round data lost!");
        }
        PocMeetingMember member = localRound.getMember(roundData.getPackingIndexOfRound());
        //todo 计算共识奖励并对比
        CoinBaseTransaction coinBaseTx = (CoinBaseTransaction) tx;

        CoinBaseTransaction checkCoinBaseTx = createNewCoinBaseTx(member, txs.subList(1, txs.size()), localRound);
        if (checkCoinBaseTx.getHash().equals(coinBaseTx.getHash())) {
            return ValidateResult.getSuccessResult();
        }

        return ValidateResult.getFailedResult("Consensus reward calculation error.");
    }

    public CoinBaseTransaction createNewCoinBaseTx(PocMeetingMember member, List<Transaction> txList, PocMeetingRound localRound) {
        CoinTransferData data = new CoinTransferData(OperationType.COIN_BASE, Na.ZERO);
        List<ConsensusReward> rewardList = calcReward(txList, member, localRound);
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
            tx.setTime(member.getPackTime());
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        tx.setFee(Na.ZERO);
        tx.setHash(NulsDigestData.calcDigestData(tx));
        return tx;
    }

    private List<ConsensusReward> calcReward(List<Transaction> txList, PocMeetingMember self, PocMeetingRound localRound) {
        List<ConsensusReward> rewardList = new ArrayList<>();
        if (self.getOwnDeposit().getValue() == Na.ZERO.getValue()) {
            long totalFee = 0;
            for (Transaction tx : txList) {
                totalFee += tx.getFee().getValue();
            }
            if (totalFee == 0L) {
                return rewardList;
            }
            double caReward = totalFee;
            ConsensusReward agentReword = new ConsensusReward();
            agentReword.setAddress(self.getAgentAddress());
            agentReword.setReward(Na.valueOf((long) caReward));
            rewardList.add(agentReword);
            return rewardList;
        }
        long totalFee = 0;
        for (Transaction tx : txList) {
            totalFee += tx.getFee().getValue();
        }
        double totalAll = DoubleUtils.mul(localRound.getMemberCount(), PocConsensusConstant.BLOCK_REWARD.getValue());

        double blockReword = totalFee + DoubleUtils.mul(totalAll, DoubleUtils.div(self.getOwnDeposit().getValue() + self.getTotalDeposit().getValue(), localRound.getTotalDeposit().getValue()));

        ConsensusReward agentReword = new ConsensusReward();
        agentReword.setAddress(self.getAgentAddress());
        double caReward = DoubleUtils.mul(blockReword, DoubleUtils.mul(DoubleUtils.div((localRound.getTotalDeposit().getValue() - self.getOwnDeposit().getValue()), localRound.getTotalDeposit().getValue()
        ), DoubleUtils.round(self.getCommissionRate() / 100, 2)));
        agentReword.setReward(Na.valueOf((long) caReward));
        Map<String, ConsensusReward> rewardMap = new HashMap<>();
        rewardMap.put(self.getAgentAddress(), agentReword);
        double delegateCommissionRate = DoubleUtils.div((100 - self.getCommissionRate()), 100, 2);
        for (Consensus<Deposit> cd : self.getDepositList()) {
            double reward =
                    DoubleUtils.mul(DoubleUtils.mul(blockReword, delegateCommissionRate),
                            DoubleUtils.div(cd.getExtend().getDeposit().getValue(),
                                    localRound.getTotalDeposit().getValue()));

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

    private void recalc(Block bestBlock) {
        this.currentRound = null;
        this.calc(bestBlock);
    }

    /**
     * Whenever the latest height changes, calculate whether the current rotation information needs to be modified, and update the memory content if necessary.
     *
     * @param bestBlock The local view of the latest height, including a short split of my own, is likely to be higher than the latest local level.
     * @return
     */
    public synchronized void calc(Block bestBlock) {
        long bestHeight = bestBlock.getHeader().getHeight();
        BlockRoundData bestRoundData = new BlockRoundData(bestBlock.getHeader().getExtend());
        if (null != currentRound && currentRound.getIndex() == bestRoundData.getRoundIndex() && bestRoundData.getPackingIndexOfRound() == bestRoundData.getConsensusMemberCount()) {
            PocMeetingRound previousRound = currentRound;
            this.currentRound = calcNextRound(bestBlock.getHeader(), bestHeight, bestRoundData);
            if (previousRound.getIndex() != (currentRound.getIndex() - 1)) {
                previousRound = calcCurrentRound(bestBlock.getHeader(), bestHeight, bestRoundData);
                this.currentRound.setPreRound(previousRound);
            }
        } else if (null != currentRound && currentRound.getIndex() == bestRoundData.getRoundIndex()) {
            if (TimeService.currentTimeMillis() > currentRound.getEndTime()) {
                PocMeetingRound previousRound = currentRound;
                this.currentRound = calcNextRound(bestBlock.getHeader(), bestHeight, bestRoundData);
                if (previousRound.getIndex() != (currentRound.getIndex() - 1)) {
                    previousRound = calcCurrentRound(bestBlock.getHeader(), bestHeight, bestRoundData);
                    this.currentRound.setPreRound(previousRound);
                }
            } else {
                return;
            }
        } else if (null != currentRound && currentRound.getIndex() > bestRoundData.getRoundIndex()) {
            this.recalc(bestBlock);
            return;
        } else if (null != currentRound && currentRound.getIndex() < bestRoundData.getRoundIndex()) {
            this.recalc(bestBlock);
            return;
        } else if (null == currentRound && bestRoundData.getPackingIndexOfRound() == bestRoundData.getConsensusMemberCount()) {
            PocMeetingRound previousRound = calcCurrentRound(bestBlock.getHeader(), bestHeight, bestRoundData);
            this.currentRound = calcNextRound(bestBlock.getHeader(), bestHeight, bestRoundData);
            this.currentRound.setPreRound(previousRound);
        } else if (null == currentRound) {
            PocMeetingRound previousRound = calcPreviousRound(bestBlock.getHeader(), bestHeight, bestRoundData);
            this.currentRound = calcCurrentRound(bestBlock.getHeader(), bestHeight, bestRoundData);
            this.currentRound.setPreRound(previousRound);
        }
        List<Account> accountList = accountService.getAccountList();
        this.currentRound.calcLocalPacker(accountList);
    }

    private PocMeetingRound calcPreviousRound(BlockHeader header, long bestHeight, BlockRoundData bestRoundData) {
        if (bestHeight == 0) {
            return null;
        }
        PocMeetingRound round = new PocMeetingRound();
        round.setIndex(bestRoundData.getRoundIndex() - 1);
        long calcHeight = 0L;
        boolean needSetStartTime = true;
        if (bestHeight - PocConsensusConstant.CONFIRM_BLOCK_COUNT > 0 && round.getIndex() > 1) {
            Block lastBlock = getBlockService().getRoundLastBlockFromDb(round.getIndex() - 1);
            if (null == lastBlock) {
                long height = bestHeight - 1;
                while (true) {
                    Block block = getBlockService().getBlock(height);
                    height--;
                    BlockRoundData blockRoundData = new BlockRoundData(block.getHeader().getExtend());
                    if (blockRoundData.getRoundIndex() <= (round.getIndex() - 1)) {
                        lastBlock = block;
                        round.setStartTime(blockRoundData.getRoundEndTime());
                        break;
                    }
                }
            }
            calcHeight = lastBlock.getHeader().getHeight();
            needSetStartTime = false;
        }
        List<PocMeetingMember> memberList;
        if (calcHeight == (0 - PocConsensusConstant.CONFIRM_BLOCK_COUNT)) {
            memberList = getFirstMemberList(header);
        } else {
            memberList = getMemberList(calcHeight, round, header);
        }

        Collections.sort(memberList);
        if (needSetStartTime) {
            round.setStartTime(bestRoundData.getRoundEndTime() - PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000 * memberList.size());
        }
        round.setMemberList(memberList);
        round.setMemberCount(memberList.size());
        return round;
    }

    private PocMeetingRound calcNextRound(BlockHeader bestBlockHeader, long bestHeight, BlockRoundData bestRoundData) {
        PocMeetingRound round = new PocMeetingRound();
        round.setIndex(bestRoundData.getRoundIndex() + 1);
        round.setStartTime(bestRoundData.getRoundEndTime());
        long calcHeight = 0L;
        if (bestRoundData.getPackingIndexOfRound() == bestRoundData.getConsensusMemberCount() || NulsContext.getInstance().getBestHeight() <= bestHeight) {
            calcHeight = bestHeight - PocConsensusConstant.CONFIRM_BLOCK_COUNT;
        } else {
            Block bestBlock = NulsContext.getInstance().getBestBlock();
            if (null == bestBlock) {
                throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "best block is null");
            }
            BlockRoundData localBestRoundData = new BlockRoundData(bestBlock.getHeader().getExtend());
            if (localBestRoundData.getRoundIndex() == bestRoundData.getRoundIndex() && localBestRoundData.getPackingIndexOfRound() != localBestRoundData.getConsensusMemberCount()) {
                throw new NulsRuntimeException(ErrorCode.FAILED, "The next round of information is not yet available.");
            } else if (localBestRoundData.getRoundIndex() == bestRoundData.getRoundIndex() && localBestRoundData.getPackingIndexOfRound() == localBestRoundData.getConsensusMemberCount()) {
                return calcNextRound(bestBlock.getHeader(), bestBlock.getHeader().getHeight(), localBestRoundData);
            } else {
                Block nextRoundFirstBlock = getBlockService().getRoundFirstBlockFromDb(round.getIndex());
                if (null == nextRoundFirstBlock) {
                    long height = bestHeight + 1;
                    while (true) {
                        Block block = getBlockService().getBlock(height);
                        height++;
                        BlockRoundData blockRoundData = new BlockRoundData(block.getHeader().getExtend());
                        if (blockRoundData.getRoundIndex() == round.getIndex()) {
                            nextRoundFirstBlock = block;
                            break;
                        }
                    }
                }
                calcHeight = nextRoundFirstBlock.getHeader().getHeight() - PocConsensusConstant.CONFIRM_BLOCK_COUNT - 1;
            }
        }

        List<PocMeetingMember> memberList = getMemberList(calcHeight, round, bestBlockHeader);

        Collections.sort(memberList);

        round.setMemberList(memberList);

        round.setMemberCount(memberList.size());
        boolean b = false;
        while (round.getEndTime() < TimeService.currentTimeMillis()) {
            long time = TimeService.currentTimeMillis() - round.getStartTime();
            long roundTime = PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L * round.getMemberCount();
            long index = time / roundTime;
            long startTime = round.getStartTime() + index * roundTime;
            round.setStartTime(startTime);
            round.setIndex(bestRoundData.getRoundIndex() + index);
            b = true;
        }
        if (b) {
            for (PocMeetingMember member : memberList) {
                member.setRoundIndex(round.getIndex());
                member.setRoundStartTime(round.getStartTime());
                member.setSortValue(bestRoundData.getRoundEndTime() + "");
            }
        }
        return round;
    }

    public List<PocMeetingMember> getFirstMemberList(BlockHeader header) {
        List<PocMeetingMember> memberList = new ArrayList<>();
        PocMeetingMember member = new PocMeetingMember();
        member.setPackingAddress(header.getPackingAddress());
        member.setAgentAddress(header.getPackingAddress());
        member.setCreditVal(1);
        memberList.add(member);
        return memberList;
    }


    private PocMeetingRound calcCurrentRound(BlockHeader bestBlockHeader, long bestHeight, BlockRoundData bestRoundData) {
        PocMeetingRound round = new PocMeetingRound();
        round.setIndex(bestRoundData.getRoundIndex());
        round.setMemberCount(bestRoundData.getConsensusMemberCount());
        round.setStartTime(bestRoundData.getRoundStartTime());
        List<PocMeetingMember> memberList;
        if (bestHeight == 0) {
            memberList = getFirstMemberList(bestBlockHeader);
        } else {
            long calcHeight = 0L;
            if (bestHeight - PocConsensusConstant.CONFIRM_BLOCK_COUNT > 1) {
                if (bestRoundData.getPackingIndexOfRound() == 1) {
                    calcHeight = bestHeight - PocConsensusConstant.CONFIRM_BLOCK_COUNT - 1;
                } else {
                    Block firstBlock = getBlockService().getRoundFirstBlockFromDb(round.getIndex());
                    if (null == firstBlock) {
                        long height = bestHeight - bestRoundData.getPackingIndexOfRound() + 1;
                        while (true) {
                            BlockRoundData blockRoundData;
                            if (height == bestBlockHeader.getHeight()) {
                                blockRoundData = bestRoundData;
                            } else {
                                firstBlock = getBlockService().getBlock(height);
                                blockRoundData = new BlockRoundData(firstBlock.getHeader().getExtend());
                            }
                            height++;
                            if (blockRoundData.getRoundIndex() == round.getIndex()) {
                                break;
                            }
                        }

                    }
                    calcHeight = firstBlock.getHeader().getHeight() - PocConsensusConstant.CONFIRM_BLOCK_COUNT - 1;
                }
            }
            memberList = getMemberList(calcHeight, round, bestBlockHeader);
        }
        Collections.sort(memberList);

        round.setMemberList(memberList);
        if (round.getMemberCount() != memberList.size()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "members size is wrong!");
        }
        return round;
    }

    private List<PocMeetingMember> getMemberList(long calcHeight, PocMeetingRound round, BlockHeader bestBlockHeader) {
        List<PocMeetingMember> memberList = new ArrayList<>();
        Na totalDeposit = Na.ZERO;
        List<PocMeetingMember> seedMemberList = getDefaultSeedList();
        for (PocMeetingMember member : seedMemberList) {
            member.setRoundStartTime(round.getStartTime());
            member.setRoundIndex(round.getIndex());
            memberList.add(member);
        }
        List<Consensus<Agent>> agentList = getAgentList(calcHeight);
        Map<String, List<DepositPo>> depositMap = new HashMap<>();
        if (agentList.size() > 0) {
            List<DepositPo> depositPoList = depositDataService.getAllList(calcHeight);
            for (DepositPo depositPo : depositPoList) {
                List<DepositPo> subList = depositMap.get(depositPo.getAgentHash());
                if (null == subList) {
                    subList = new ArrayList<>();
                    depositMap.put(depositPo.getAgentHash(), subList);
                }
                subList.add(depositPo);
            }
        }
        Set<String> agentSet = consensusCacheManager.agentKeySet();
        Set<String> depositKeySet = consensusCacheManager.depositKeySet();

        for (Consensus<Agent> ca : agentList) {
            PocMeetingMember member = new PocMeetingMember();
            member.setAgentConsensus(ca);
            member.setRoundIndex(round.getIndex());
            member.setAgentHash(ca.getHexHash());
            member.setAgentAddress(ca.getAddress());
            member.setPackingAddress(ca.getExtend().getPackingAddress());
            member.setRoundStartTime(round.getStartTime());
            member.setOwnDeposit(ca.getExtend().getDeposit());
            member.setCommissionRate(ca.getExtend().getCommissionRate());
            totalDeposit = totalDeposit.add(ca.getExtend().getDeposit());
            List<DepositPo> depositPoList = depositMap.get(ca.getHexHash());
            if (depositPoList != null) {
                List<Consensus<Deposit>> cdlist = new ArrayList<>();
                for (DepositPo depositPo : depositPoList) {
                    Consensus<Deposit> cd = ConsensusTool.fromPojo(depositPo);
                    member.setTotalDeposit(member.getTotalDeposit().add(cd.getExtend().getDeposit()));
                    cdlist.add(cd);
                }
                member.setDepositList(cdlist);
            }
            totalDeposit = totalDeposit.add(member.getTotalDeposit());
            member.setCreditVal(calcCreditVal(member, round.getIndex() - 2));
            if (member.getTotalDeposit().isGreaterThan(PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT)) {
                ca.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
                memberList.add(member);
            } else {
                ca.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
            }
            consensusCacheManager.cacheAgent(ca);
            agentSet.remove(ca.getHexHash());
            for (Consensus<Deposit> cd : member.getDepositList()) {
                cd.getExtend().setStatus(ca.getExtend().getStatus());
                consensusCacheManager.cacheDeposit(cd);
                depositKeySet.remove(cd.getHexHash());
            }
        }
        for (String key : agentSet) {
            consensusCacheManager.removeAgent(key);
        }
        for (String key : depositKeySet) {
            consensusCacheManager.removeDeposit(key);
        }
        round.setTotalDeposit(totalDeposit);
        Collections.sort(memberList);
        return memberList;
    }

    private double calcCreditVal(PocMeetingMember member, long calcRoundIndex) {
        if (calcRoundIndex == 0) {
            return 0;
        }
        long roundStart = calcRoundIndex - PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        long blockCount = pocBlockService.getBlockCount(member.getAgentAddress(), roundStart, calcRoundIndex);
        long sumRoundVal = pocBlockService.getSumOfRoundIndexOfYellowPunish(member.getAgentAddress(), roundStart, calcRoundIndex);
        double ability = blockCount / PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;

        double penalty = (PocConsensusConstant.CREDIT_MAGIC_NUM * sumRoundVal) / (PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT * PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);
        return ability - penalty;
    }

    private List<Consensus<Agent>> getAgentList(long calcHeight) {
        List<AgentPo> poList = agentDataService.getAllList(calcHeight);
        if (null == poList || poList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Consensus<Agent>> agentList = new ArrayList<>();
        for (AgentPo po : poList) {
            Consensus<Agent> agent = ConsensusTool.fromPojo(po);
            agentList.add(agent);
        }
        return agentList;
    }

    private List<PocMeetingMember> getDefaultSeedList() {
        if (null != seedList) {
            return seedList;
        }
        List<PocMeetingMember> seedList = new ArrayList<>();
        if (csManager.getSeedNodeList() == null) {
            return seedList;
        }
        for (String address : csManager.getSeedNodeList()) {
            PocMeetingMember member = new PocMeetingMember();
            member.setAgentAddress(address);
            member.setPackingAddress(address);
            member.setCreditVal(1);
            seedList.add(member);
        }
        this.seedList = seedList;
        return seedList;
    }

    public PocMeetingRound getCurrentRound() {
        return currentRound;
    }

    private PocMeetingRound getRoundDataOrCalc(BlockHeader header, long height, BlockRoundData roundData) {
        PocMeetingRound round = getRoundData(roundData.getRoundIndex());
        if (null != round) {
            return round;
        }
        return this.calcCurrentRound(header, height, roundData);
    }


    private PocMeetingRound getRoundData(long roundIndex) {
        if (null == currentRound) {
            return null;
        }
        if (roundIndex == currentRound.getIndex()) {
            return currentRound;
        }
        if (null != currentRound.getPreRound() && roundIndex == currentRound.getPreRound().getIndex()) {
            return currentRound.getPreRound();
        }
        return null;
    }

    public boolean isLocalHasSeed(List<Account> accountList) {
        for (Account account : accountList) {
            for (PocMeetingMember seed : this.getDefaultSeedList()) {
                if (seed.getAgentAddress().equals(account.getAddress().getBase58())) {
                    return true;
                }
            }
        }
        return false;
    }

    private BlockService getBlockService() {
        if (null == this.consensusBlockService) {
            consensusBlockService = NulsContext.getServiceBean(BlockService.class);
        }
        return consensusBlockService;
    }
}

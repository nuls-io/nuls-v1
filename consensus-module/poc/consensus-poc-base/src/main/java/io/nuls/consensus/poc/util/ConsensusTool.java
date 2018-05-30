/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.consensus.poc.util;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.service.AccountService;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.model.BlockData;
import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.entity.YellowPunishData;
import io.nuls.consensus.poc.protocol.tx.YellowPunishTransaction;
import io.nuls.core.tools.calc.DoubleUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.model.SmallBlock;
import io.nuls.protocol.model.tx.CoinBaseTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ConsensusTool {

    private static AccountService accountService = NulsContext.getServiceBean(AccountService.class);

    public static SmallBlock getSmallBlock(Block block) {
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setHeader(block.getHeader());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (Transaction tx : block.getTxs()) {
            txHashList.add(tx.getHash());
            if (tx.getType() == ProtocolConstant.TX_TYPE_COINBASE ||
                    tx.getType() == ConsensusConstant.TX_TYPE_YELLOW_PUNISH ||
                    tx.getType() == ConsensusConstant.TX_TYPE_RED_PUNISH) {
                smallBlock.addBaseTx(tx);
            }
        }
        smallBlock.setTxHashList(txHashList);
        return smallBlock;
    }

    public static Block createBlock(BlockData blockData, Account account) throws NulsException {
        if (null == account) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }

        // Account cannot be encrypted, otherwise it will be wrong
        // 账户不能加密，否则抛错
        Result result = accountService.isEncrypted(account);
        if (result.isSuccess()) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_IS_ALREADY_ENCRYPTED);
        }

        Block block = new Block();
        block.setTxs(blockData.getTxList());
        BlockHeader header = new BlockHeader();
        block.setHeader(header);
        try {
            block.getHeader().setExtend(blockData.getRoundData().serialize());
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        header.setHeight(blockData.getHeight());
        header.setTime(blockData.getTime());
        header.setPreHash(blockData.getPreHash());
        header.setTxCount(blockData.getTxList().size());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (int i = 0; i < blockData.getTxList().size(); i++) {
            Transaction tx = blockData.getTxList().get(i);
            tx.setBlockHeight(header.getHeight());
            txHashList.add(tx.getHash());
        }
        header.setMerkleHash(NulsDigestData.calcMerkleDigestData(txHashList));
        header.setHash(NulsDigestData.calcDigestData(block.getHeader()));

        P2PKHScriptSig scriptSig = new P2PKHScriptSig();

        NulsSignData signData = accountService.signDigest(header.getHash().getDigestBytes(), account.getEcKey());
        scriptSig.setSignData(signData);
        scriptSig.setPublicKey(account.getPubKey());
        header.setScriptSig(scriptSig);

        return block;
    }

    public static CoinBaseTransaction createCoinBaseTx(MeetingMember member, List<Transaction> txList, MeetingRound localRound, long unlockHeight) {
        CoinData coinData = new CoinData();
        List<Coin> rewardList = calcReward(txList, member, localRound, unlockHeight);
        for (Coin coin : rewardList) {
            coinData.addTo(coin);
        }
        CoinBaseTransaction tx = new CoinBaseTransaction();
        tx.setTime(member.getPackEndTime());
        tx.setCoinData(coinData);
        try {
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        } catch (IOException e) {
            Log.error(e);
        }
        return tx;
    }

    private static List<Coin> calcReward(List<Transaction> txList, MeetingMember self, MeetingRound localRound, long unlockHeight) {
        List<Coin> rewardList = new ArrayList<>();
        if (self.getOwnDeposit().getValue() == Na.ZERO.getValue()) {
            long totalFee = 0;
            for (Transaction tx : txList) {
                totalFee += tx.getFee().getValue();
            }
            if (totalFee == 0L) {
                return rewardList;
            }
            double caReward = totalFee;
            Coin agentReword = new Coin(self.getRewardAddress(), Na.valueOf((long) caReward), unlockHeight);
            rewardList.add(agentReword);
            return rewardList;
        }
        long totalFee = 0;
        for (Transaction tx : txList) {
            totalFee += tx.getFee().getValue();
        }
        double totalAll = DoubleUtils.mul(localRound.getMemberCount(), PocConsensusConstant.BLOCK_REWARD.getValue());
        double commissionRate = DoubleUtils.div(self.getCommissionRate(), 100, 2);
        double agentWeight = DoubleUtils.mul(DoubleUtils.sum(self.getOwnDeposit().getValue(), self.getTotalDeposit().getValue()), self.getCalcCreditVal());
        double blockReword = totalFee;
        if (localRound.getTotalWeight() > 0d && agentWeight > 0d) {
            blockReword = DoubleUtils.sum(blockReword, DoubleUtils.mul(totalAll, DoubleUtils.div(agentWeight, localRound.getTotalWeight())));
        }

        if (blockReword == 0d) {
            return rewardList;
        }

        long realTotalAllDeposit = self.getOwnDeposit().getValue() + self.getTotalDeposit().getValue();
        double caReward = DoubleUtils.mul(blockReword, DoubleUtils.div(self.getOwnDeposit().getValue(), realTotalAllDeposit));

        for (Deposit deposit : self.getDepositList()) {
            double weight = DoubleUtils.div(deposit.getDeposit().getValue(), realTotalAllDeposit);
            if (Arrays.equals(deposit.getAddress(), self.getAgentAddress())) {
                caReward = caReward + DoubleUtils.mul(blockReword, weight);
            } else {
                double reward = DoubleUtils.mul(blockReword, weight);
                double fee = DoubleUtils.mul(reward, commissionRate);
                caReward = caReward + fee;
                double hisReward = DoubleUtils.sub(reward, fee);
                Na depositReward = Na.valueOf(DoubleUtils.longValue(hisReward));

                Coin rewardCoin = null;

                for (Coin coin : rewardList) {
                    if (Arrays.equals(coin.getOwner(), deposit.getAddress())) {
                        rewardCoin = coin;
                        break;
                    }
                }
                if (rewardCoin == null) {
                    rewardCoin = new Coin(deposit.getAddress(), depositReward, unlockHeight);
                    rewardList.add(rewardCoin);
                } else {
                    rewardCoin.setNa(rewardCoin.getNa().add(depositReward));
                }
            }
        }

        rewardList.sort(new Comparator<Coin>() {
            @Override
            public int compare(Coin o1, Coin o2) {
                return Arrays.hashCode(o1.getOwner()) > Arrays.hashCode(o2.getOwner()) ? 1 : -1;
            }
        });

        Coin agentReword = new Coin(self.getRewardAddress(), Na.valueOf(DoubleUtils.longValue(caReward)), unlockHeight);
        rewardList.add(0, agentReword);

        return rewardList;
    }


    public static YellowPunishTransaction createYellowPunishTx(Block preBlock, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        BlockRoundData preBlockRoundData = new BlockRoundData(preBlock.getHeader().getExtend());
        if (self.getRoundIndex() - preBlockRoundData.getRoundIndex() > 1) {
            return null;
        }

        int yellowCount = 0;
        if (self.getRoundIndex() == preBlockRoundData.getRoundIndex() && self.getPackingIndexOfRound() != preBlockRoundData.getPackingIndexOfRound() + 1) {
            yellowCount = self.getPackingIndexOfRound() - preBlockRoundData.getPackingIndexOfRound() - 1;
        }

        if (self.getRoundIndex() != preBlockRoundData.getRoundIndex() && (self.getPackingIndexOfRound() != 1 || preBlockRoundData.getPackingIndexOfRound() != preBlockRoundData.getConsensusMemberCount())) {
            yellowCount = self.getPackingIndexOfRound() + preBlockRoundData.getConsensusMemberCount() - preBlockRoundData.getPackingIndexOfRound() - 1;
        }

        if (yellowCount == 0) {
            return null;
        }

        List<byte[]> addressList = new ArrayList<>();
        for (int i = 1; i <= yellowCount; i++) {
            int index = self.getPackingIndexOfRound() - i;
            if (index > 0) {
                addressList.add(round.getMember(index).getAgentAddress());
            } else {
                MeetingRound preRound = round.getPreRound();
                addressList.add(preRound.getMember(index + preRound.getMemberCount()).getAgentAddress());
            }
        }
        if (addressList.isEmpty()) {
            return null;
        }
        YellowPunishTransaction punishTx = new YellowPunishTransaction();
        YellowPunishData data = new YellowPunishData();
        data.setAddressList(addressList);
        punishTx.setTxData(data);
        punishTx.setTime(self.getPackEndTime());
        punishTx.setHash(NulsDigestData.calcDigestData(punishTx.serializeForHash()));
        return punishTx;
    }

}


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
package io.nuls.consensus.utils;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.*;
import io.nuls.consensus.entity.block.BlockData;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.ConsensusReward;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.script.P2PKHScriptSig;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.params.OperationType;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ConsensusTool {

    private static AccountService accountService = NulsContext.getServiceBean(AccountService.class);

    public static final BlockHeaderPo toPojo(BlockHeader header) {
        BlockHeaderPo po = new BlockHeaderPo();
        po.setTxCount(header.getTxCount());
        po.setPreHash(header.getPreHash().getDigestHex());
        po.setMerkleHash(header.getMerkleHash().getDigestHex());
        po.setHeight(header.getHeight());
        po.setCreateTime(header.getTime());
        po.setHash(header.getHash().getDigestHex());
        po.setSize(header.getSize());
        if (null != header.getScriptSig()) {
            try {
                po.setScriptSig(header.getScriptSig().serialize());
            } catch (IOException e) {
                Log.error(e);
            }
        }
        po.setTxCount(header.getTxCount());
        po.setConsensusAddress(header.getPackingAddress());
        po.setExtend(header.getExtend());
        BlockRoundData data = new BlockRoundData();
        try {
            data.parse(header.getExtend());
        } catch (NulsException e) {
            Log.error(e);
        }
        po.setRoundIndex(data.getRoundIndex());
        return po;
    }

    public static final BlockHeader fromPojo(BlockHeaderPo po) throws NulsException {
        if (null == po) {
            return null;
        }
        BlockHeader header = new BlockHeader();
        header.setHash(NulsDigestData.fromDigestHex(po.getHash()));
        header.setMerkleHash(NulsDigestData.fromDigestHex(po.getMerkleHash()));
        header.setPackingAddress(po.getConsensusAddress());
        header.setTxCount(po.getTxCount());
        header.setPreHash(NulsDigestData.fromDigestHex(po.getPreHash()));
        header.setTime(po.getCreateTime());
        header.setHeight(po.getHeight());
        header.setExtend(po.getExtend());
        header.setSize(po.getSize());
        header.setScriptSig((new NulsByteBuffer(po.getScriptSig()).readNulsData(new P2PKHScriptSig())));
        return header;
    }

    public static Consensus<Agent> fromPojo(AgentPo po) {
        if (null == po) {
            return null;
        }
        Agent agent = new Agent();
        agent.setDeposit(Na.valueOf(po.getDeposit()));
        agent.setCommissionRate(po.getCommissionRate());
        agent.setPackingAddress(po.getPackingAddress());
        agent.setIntroduction(po.getRemark());
        agent.setStartTime(po.getStartTime());
        agent.setStatus(po.getStatus());
        agent.setAgentName(po.getAgentName());
        agent.setBlockHeight(po.getBlockHeight());
        Consensus<Agent> ca = new ConsensusAgentImpl();
        ca.setDelHeight(po.getDelHeight());
        ca.setAddress(po.getAgentAddress());
        ca.setHash(NulsDigestData.fromDigestHex(po.getId()));
        ca.setExtend(agent);
        return ca;
    }

    public static Consensus<Deposit> fromPojo(DepositPo po) {
        if (null == po) {
            return null;
        }
        Consensus<Deposit> ca = new ConsensusDepositImpl();
        ca.setAddress(po.getAddress());
        ca.setDelHeight(po.getDelHeight());
        Deposit deposit = new Deposit();
        deposit.setAgentHash(po.getAgentHash());
        deposit.setDeposit(Na.valueOf(po.getDeposit()));
        deposit.setStartTime(po.getTime());
        deposit.setTxHash(po.getTxHash());
        deposit.setBlockHeight(po.getBlockHeight());
        ca.setHash(NulsDigestData.fromDigestHex(po.getId()));
        ca.setExtend(deposit);
        return ca;
    }

    public static AgentPo agentToPojo(Consensus<Agent> bean) {
        if (null == bean) {
            return null;
        }
        AgentPo po = new AgentPo();
        po.setAgentAddress(bean.getAddress());
        po.setBlockHeight(bean.getExtend().getBlockHeight());
        po.setId(bean.getHexHash());
        po.setDeposit(bean.getExtend().getDeposit().getValue());
        po.setStartTime(bean.getExtend().getStartTime());
        po.setRemark(bean.getExtend().getIntroduction());
        po.setPackingAddress(bean.getExtend().getPackingAddress());
        po.setStatus(bean.getExtend().getStatus());
        po.setAgentName(bean.getExtend().getAgentName());
        po.setCommissionRate(bean.getExtend().getCommissionRate());
        po.setDelHeight(bean.getDelHeight());
        return po;
    }

    public static DepositPo depositToPojo(Consensus<Deposit> bean, String txHash) {
        if (null == bean) {
            return null;
        }
        DepositPo po = new DepositPo();
        po.setAddress(bean.getAddress());
        po.setDeposit(bean.getExtend().getDeposit().getValue());
        po.setTime(bean.getExtend().getStartTime());
        po.setAgentHash(bean.getExtend().getAgentHash());
        po.setId(bean.getHexHash());
        po.setTxHash(txHash);
        po.setDelHeight(bean.getDelHeight());
        po.setBlockHeight(bean.getExtend().getBlockHeight());
        return po;
    }

    public static Block createBlock(BlockData blockData, Account account) throws NulsException {
        if (null == account) {
            throw new NulsRuntimeException(ErrorCode.ACCOUNT_NOT_EXIST);
        }
        Block block = new Block();
        block.setTxs(blockData.getTxList());
        BlockHeader header = new BlockHeader();
        block.setHeader(header);
        try {
            block.getHeader().setExtend(blockData.getRoundData().serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        header.setHeight(blockData.getHeight());
        header.setTime(TimeService.currentTimeMillis());
        header.setPreHash(blockData.getPreHash());
        header.setTxCount(blockData.getTxList().size());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (int i = 0; i < blockData.getTxList().size(); i++) {
            Transaction tx = blockData.getTxList().get(i);
            txHashList.add(tx.getHash());
        }
        header.setPackingAddress(account.getAddress().toString());
        header.setMerkleHash(NulsDigestData.calcMerkleDigestData(txHashList));
        header.setHash(NulsDigestData.calcDigestData(block.getHeader()));
        P2PKHScriptSig scriptSig = new P2PKHScriptSig();
        NulsSignData signData = accountService.signDigest(header.getHash(), account, NulsContext.getCachedPasswordOfWallet());
        scriptSig.setSignData(signData);
        scriptSig.setPublicKey(account.getPubKey());
        header.setScriptSig(scriptSig);
        return block;
    }


    public static CoinBaseTransaction createCoinBaseTx(PocMeetingMember member, List<Transaction> txList, PocMeetingRound localRound) {
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
            tx.setTime(member.getPackEndTime());
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        tx.setFee(Na.ZERO);
        tx.setHash(NulsDigestData.calcDigestData(tx));
        return tx;
    }

    private static List<ConsensusReward> calcReward(List<Transaction> txList, PocMeetingMember self, PocMeetingRound localRound) {
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
        double commissionRate = DoubleUtils.div(self.getCommissionRate(), 100, 2);
        double agentWeight = DoubleUtils.mul(self.getOwnDeposit().getValue() + self.getTotalDeposit().getValue(), self.getCalcCreditVal());
        double blockReword = totalFee;
        if (localRound.getTotalWeight() > 0d && agentWeight > 0d) {
            blockReword = DoubleUtils.sum(blockReword, DoubleUtils.mul(totalAll, DoubleUtils.div(agentWeight, localRound.getTotalWeight())));
        }

        if(blockReword == 0d) {
            return rewardList;
        }

        ConsensusReward agentReword = new ConsensusReward();
        agentReword.setAddress(self.getAgentAddress());

        long realTotalAllDeposit =  self.getOwnDeposit().getValue()+self.getTotalDeposit().getValue();
        double caReward = DoubleUtils.mul(blockReword, DoubleUtils.div(self.getOwnDeposit().getValue(),realTotalAllDeposit));

        Map<String, ConsensusReward> rewardMap = new HashMap<>();
        for (Consensus<Deposit> cd : self.getDepositList()) {
            double weight = DoubleUtils.div(cd.getExtend().getDeposit().getValue(),realTotalAllDeposit);
            if (cd.getAddress().equals(self.getAgentAddress())) {
                caReward = caReward + DoubleUtils.mul(blockReword,weight );
            } else {
                ConsensusReward depositReward = rewardMap.get(cd.getAddress());
                if (null == depositReward) {
                    depositReward = new ConsensusReward();
                    depositReward.setAddress(cd.getAddress());
                    rewardMap.put(cd.getAddress(), depositReward);
                }
                double reward = DoubleUtils.mul(blockReword, weight);
                double fee = DoubleUtils.mul(reward, commissionRate);
                caReward = caReward + fee;
                double hisReward = DoubleUtils.sub(reward, fee);
                depositReward.setReward(depositReward.getReward().add(Na.valueOf(DoubleUtils.longValue(hisReward))));
            }
        }
        agentReword.setReward(Na.valueOf(DoubleUtils.longValue(caReward)));
        rewardMap.put(self.getAgentAddress(), agentReword);
        rewardList.addAll(rewardMap.values());
        return rewardList;
    }


    public static YellowPunishTransaction createYellowPunishTx(Block preBlock, PocMeetingMember self, PocMeetingRound round) throws NulsException, IOException {
        BlockRoundData preBlockRoundData = new BlockRoundData(preBlock.getHeader().getExtend());
        if (self.getRoundIndex() - preBlockRoundData.getRoundIndex() >= 2) {
            return null;
        }

        int yellowCount = 0;
        if(self.getRoundIndex() == preBlockRoundData.getRoundIndex() && self.getPackingIndexOfRound() != preBlockRoundData.getPackingIndexOfRound() + 1) {
            yellowCount = self.getPackingIndexOfRound() - preBlockRoundData.getPackingIndexOfRound() - 1;
        }

        if(self.getRoundIndex() != preBlockRoundData.getRoundIndex() && (self.getPackingIndexOfRound() != 1 || preBlockRoundData.getPackingIndexOfRound() != preBlockRoundData.getConsensusMemberCount())) {
            yellowCount = self.getPackingIndexOfRound() + preBlockRoundData.getConsensusMemberCount() - preBlockRoundData.getPackingIndexOfRound() -1;
        }

        if(yellowCount == 0) {
            return null;
        }

        List<Address> addressList = new ArrayList<>();
        for(int i = 1 ; i <= yellowCount ; i++) {
            int index = self.getPackingIndexOfRound() - i;
            if(index > 0) {
                addressList.add(Address.fromHashs(round.getMember(index).getAgentAddress()));
            } else {
                PocMeetingRound preRound = round.getPreRound();
                addressList.add(Address.fromHashs(preRound.getMember(index + preRound.getMemberCount()).getAgentAddress()));
            }
        }
        if (addressList.isEmpty()) {
            return null;
        }
        YellowPunishTransaction punishTx = new YellowPunishTransaction();
        YellowPunishData data = new YellowPunishData();
        data.setAddressList(addressList);
        data.setHeight(preBlock.getHeader().getHeight() + 1);
        punishTx.setTxData(data);
        punishTx.setTime(self.getPackEndTime());
        punishTx.setFee(Na.ZERO);
        punishTx.setHash(NulsDigestData.calcDigestData(punishTx));
//        punishTx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(punishTx.getHash(), round.getLocalPacker(), NulsContext.getCachedPasswordOfWallet()).serialize());
        return punishTx;
    }

    public static Block assemblyBlock(BlockHeader header,Map<NulsDigestData,Transaction> txMap ,List<NulsDigestData> txHashList){
        Block block = new Block();
        block.setHeader(header);
        List<Transaction> txs = new ArrayList<>();
        for (NulsDigestData txHash : txHashList) {
            Transaction tx = txMap.get(txHash);
            if (null == tx) {
                throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
            }
            txs.add(tx);
        }
        block.setTxs(txs);
        return block;
    }
}


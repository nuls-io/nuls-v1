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
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.db.entity.PunishLogPo;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.base.constant.PunishReasonEnum;
import io.nuls.protocol.base.constant.PunishType;
import io.nuls.protocol.base.entity.RedPunishData;
import io.nuls.protocol.base.entity.YellowPunishData;
import io.nuls.protocol.base.entity.block.BlockRoundData;
import io.nuls.protocol.base.entity.member.Agent;
import io.nuls.protocol.base.entity.member.Deposit;
import io.nuls.protocol.base.entity.tx.*;
import io.nuls.protocol.entity.Consensus;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class ChainContainer implements Cloneable {

    private Chain chain;

    private List<MeetingRound> roundList;

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    public ChainContainer() {}

    public ChainContainer(Chain chain) {
        this.chain = chain;
    }

    public boolean addBlock(Block block) {

        if(!chain.getEndBlockHeader().getHash().equals(block.getHeader().getPreHash())) {
            return false;
        }

        List<Block> blockList = chain.getBlockList();
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();

        List<Consensus<Agent>> agentList = chain.getAgentList();
        List<Consensus<Deposit>> depositList = chain.getDepositList();
        List<PunishLogPo> yellowList = chain.getYellowPunishList();

        long height = block.getHeader().getHeight();

        List<Transaction> txs = block.getTxs();
        for(Transaction tx : txs) {
            int txType = tx.getType();
            if(txType == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
                // Registered agent transaction
                // 注册代理交易
                RegisterAgentTransaction registerAgentTx = (RegisterAgentTransaction) tx;
                Consensus<Agent> ca = registerAgentTx.getTxData();
                agentList.add(ca);
            } else if(txType == TransactionConstant.TX_TYPE_JOIN_CONSENSUS) {
                PocJoinConsensusTransaction joinConsensusTx = (PocJoinConsensusTransaction) tx;
                Consensus<Deposit> cDeposit = joinConsensusTx.getTxData();
                depositList.add(cDeposit);
            } else if(txType == TransactionConstant.TX_TYPE_EXIT_CONSENSUS) {

                PocExitConsensusTransaction exitConsensusTx = (PocExitConsensusTransaction) tx;
                Transaction joinTx = ledgerService.getTx(exitConsensusTx.getTxData());

                if (joinTx.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT) {

                    RegisterAgentTransaction registerAgentTx = (RegisterAgentTransaction) tx;

                    Iterator<Consensus<Deposit>> it = depositList.iterator();
                    while(it.hasNext()) {
                        Consensus<Deposit> tempDe = it.next();
                        Deposit deposit = tempDe.getExtend();
                        if(deposit.getAgentHash().equals(registerAgentTx.getHash())) {
                            tempDe.setDelHeight(height);
                        }
                    }

                    Iterator<Consensus<Agent>> ita = agentList.iterator();
                    while(ita.hasNext()) {
                        Consensus<Agent> tempCa = ita.next();
                        if(tempCa.getHash().equals(registerAgentTx.getHash())) {
                            tempCa.setDelHeight(height);
                            break;
                        }
                    }
                } else {
                    PocJoinConsensusTransaction joinConsensusTx = (PocJoinConsensusTransaction) joinTx;
                    Consensus<Deposit> cDeposit = joinConsensusTx.getTxData();

                    Iterator<Consensus<Deposit>> it = depositList.iterator();
                    while(it.hasNext()) {
                        Consensus<Deposit> tempDe = it.next();
                        if(tempDe.getHash().equals(cDeposit.getHash())) {
                            tempDe.setDelHeight(height);
                            break;
                        }
                    }
                }

            } else if(txType == TransactionConstant.TX_TYPE_YELLOW_PUNISH) {

                YellowPunishData yellowPunishData = ((YellowPunishTransaction) tx).getTxData();

                List<Address> addressList = yellowPunishData.getAddressList();

                long roundIndex = new BlockRoundData(block.getHeader().getExtend()).getRoundIndex();

                for(Address address : addressList) {
                    PunishLogPo punishLogPo = new PunishLogPo();
                    punishLogPo.setHeight(yellowPunishData.getHeight());
                    punishLogPo.setAddress(address.getBase58());
                    punishLogPo.setRoundIndex(roundIndex);
                    punishLogPo.setTime(tx.getTime());
                    punishLogPo.setType(PunishType.YELLOW.getCode());

                    yellowList.add(punishLogPo);
                }

            } else if(txType == TransactionConstant.TX_TYPE_RED_PUNISH) {

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
        //TODO
        return true;
    }

    public boolean verifyAndAddBlock(Block block, boolean isDownload) {
        boolean success = verifyBlock(block, isDownload);
        if(success) {
            success = addBlock(block);
        }
        return success;
    }

    public boolean rollback() {

        List<Block> blockList = chain.getBlockList();

        if(blockList == null || blockList.size() == 0) {
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

        for(int i = agentList.size() - 1; i >= 0 ; i--) {
            Consensus<Agent> agentConsensus = agentList.get(i);
            Agent agent = agentConsensus.getExtend();

            if (agentConsensus.getDelHeight() == height) {
                agentConsensus.setDelHeight(0);
            }

            if(agent.getBlockHeight() == height) {
                depositList.remove(i);
            }
        }

        for(int i = depositList.size() - 1; i >= 0 ; i--) {
            Consensus<Deposit> tempDe = depositList.get(i);
            Deposit deposit = tempDe.getExtend();

            if (tempDe.getDelHeight() == height) {
                tempDe.setDelHeight(0);
            }

            if(deposit.getBlockHeight() == height) {
                depositList.remove(i);
            }
        }

        for(int i = yellowList.size() - 1; i >= 0 ; i--) {
            PunishLogPo tempYellow = yellowList.get(i);
            if(tempYellow.getHeight() < height) {
                break;
            }
            if (tempYellow.getHeight() == height) {
                yellowList.remove(i);
            }
        }

        for(int i = redPunishList.size() - 1; i >= 0 ; i--) {
            PunishLogPo redPunish = redPunishList.get(i);
            if(redPunish.getHeight() < height) {
                break;
            }
            if (redPunish.getHeight() == height) {
                redPunishList.remove(i);
            }
        }

        //TODO 是否需要重新计算轮次 ？

        return true;
    }

    private void resetRound() {
        //TODO
    }

    /**
     *
     * Get the state of the complete chain after the combination of a chain and the current chain bifurcation point, that is, first obtain the bifurcation point between the bifurcation chain and the current chain.
     * Then create a brand new chain, copy all the states before the bifurcation point of the main chain to the brand new chain
     *
     * 获取一条链与当前链分叉点组合之后的完整链的状态，也就是，先获取到分叉链与当前链的分叉点，
     * 然后创建一条全新的链，把主链分叉点之前的所有状态复制到全新的链
     *
     * @param chainContainer
     * @return ChainContainer
     */
    public ChainContainer getBeforeTheForkChain(ChainContainer chainContainer) {

        ChainContainer newChain = null;
        try {
            newChain = (ChainContainer) this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        // Bifurcation
        // 分叉点
        BlockHeader pointBlockHeader = chainContainer.getChain().getStartBlockHeader();

        List<Block> blockList = newChain.getChain().getBlockList();
        for(int i = blockList.size() - 1 ; i >= 0; i--) {
            Block block = blockList.get(i);
            if(pointBlockHeader.getPreHash().equals(block.getHeader().getHash())) {
                break;
            }
            newChain.rollback();
        }
        newChain.resetRound();

        return newChain;
    }

    /**
     *
     * Get the block information of the current chain and branch chain after the cross point and combine them into a new branch chain
     *
     * 获取当前链与分叉链对比分叉点之后的区块信息，组合成一个新的分叉链
     *
     * @param chainContainer
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
        for(int i = 0 ; i < blockList.size() ; i++) {

            BlockHeader blockHeader = blockHeaderList.get(i);
            if(pointBlockHeader.getPreHash().equals(blockHeader.getHash())) {
                canAdd = true;
                if(i + 1 < blockHeaderList.size()) {
                    chain.setStartBlockHeader(blockHeaderList.get(i + 1));
                    chain.setPreChainId(chainContainer.getChain().getId());
                    chain.setEndBlockHeader(getChain().getEndBlockHeader());
                }
                continue;
            }
            if(canAdd) {
                Block block = blockList.get(i);
                chain.getBlockList().add(block);
                chain.getBlockHeaderList().add(blockHeader);
            }
        }
        return new ChainContainer(chain);
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
        if(obj == null || !(obj instanceof  ChainContainer)) {
            return false;
        }
        ChainContainer other = (ChainContainer) obj;
        if(other.getChain() == null || this.chain == null) {
            return false;
        }
        return other.getChain().getId().equals(this.chain.getId());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

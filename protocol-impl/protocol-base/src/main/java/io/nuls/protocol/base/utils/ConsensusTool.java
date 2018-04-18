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
package io.nuls.protocol.base.utils;

import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.poc.protocol.model.Agent;
import io.nuls.consensus.poc.protocol.model.ConsensusAgentImpl;
import io.nuls.consensus.poc.protocol.model.ConsensusDepositImpl;
import io.nuls.consensus.poc.protocol.model.Deposit;
import io.nuls.consensus.poc.protocol.model.block.BlockRoundData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.BlockHeader;
import io.nuls.protocol.model.Na;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.script.P2PKHScriptSig;
import io.nuls.protocol.utils.io.NulsByteBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ConsensusTool {


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
        po.setConsensusAddress(Address.fromHashs(header.getPackingAddress()).getBase58());
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
        header.setPackingAddress(Address.fromHashs(po.getConsensusAddress()).getHash());
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

}


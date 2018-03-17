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
package io.nuls.consensus.utils;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusAgentImpl;
import io.nuls.consensus.entity.ConsensusDelegateImpl;
import io.nuls.consensus.entity.block.BlockData;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.script.P2PKHScriptSig;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.db.entity.DelegateAccountPo;
import io.nuls.db.entity.DelegatePo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        header.setScriptSig(( new NulsByteBuffer(po.getScriptSig()).readNulsData(new P2PKHScriptSig())));
        return header;
    }

    public static Consensus<Agent> fromPojo(DelegateAccountPo po) {
        if (null == po) {
            return null;
        }
        Agent agent = new Agent();
        agent.setStatus(ConsensusStatusEnum.WAITING.getCode());
        agent.setDeposit(Na.valueOf(po.getDeposit()));
        agent.setCommissionRate(po.getCommissionRate());
        agent.setDelegateAddress(po.getNodeAddress());
        agent.setIntroduction(po.getRemark());
        agent.setStartTime(po.getStartTime());
        agent.setStatus(po.getStatus());
        agent.setAgentName(po.getAgentName());
        Consensus<Agent> ca = new ConsensusAgentImpl();
        ca.setAddress(po.getAddress());
        ca.setExtend(agent);
        return ca;
    }

    public static Consensus<Delegate> fromPojo(DelegatePo po) {
        if (null == po) {
            return null;
        }
        Consensus<Delegate> ca = new ConsensusDelegateImpl();
        ca.setAddress(po.getAddress());
        Delegate delegate = new Delegate();
        delegate.setDelegateAddress(po.getAgentAddress());
        delegate.setDeposit(Na.valueOf(po.getDeposit()));
        delegate.setStartTime(po.getTime());
        delegate.setHash(po.getId());
        ca.setExtend(delegate);
        return ca;
    }

    public static DelegateAccountPo agentToPojo(Consensus<Agent> bean) {
        if (null == bean) {
            return null;
        }
        DelegateAccountPo po = new DelegateAccountPo();
        po.setAddress(bean.getAddress());
        po.setDeposit(bean.getExtend().getDeposit().getValue());
        po.setStartTime(bean.getExtend().getStartTime());
        po.setRemark(bean.getExtend().getIntroduction());
        po.setNodeAddress(bean.getExtend().getDelegateAddress());
        po.setId(bean.getAddress());
        po.setStatus(bean.getExtend().getStatus());
        po.setAgentName(bean.getExtend().getAgentName());
        po.setCommissionRate(bean.getExtend().getCommissionRate());
        return po;
    }

    public static DelegatePo delegateToPojo(Consensus<Delegate> bean) {
        if (null == bean) {
            return null;
        }
        DelegatePo po = new DelegatePo();
        po.setAddress(bean.getAddress());
        po.setDeposit(bean.getExtend().getDeposit().getValue());
        po.setTime(bean.getExtend().getStartTime());
        po.setAgentAddress(bean.getExtend().getDelegateAddress());
        po.setId(StringUtils.getNewUUID());
        return po;
    }

    public static Block createBlock(BlockData blockData,Account account) throws NulsException {
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
        NulsSignData signData = accountService.signDigest(header.getHash(),account,NulsContext.CACHED_PASSWORD_OF_WALLET);
        scriptSig.setSignData(signData);
        scriptSig.setPublicKey(account.getPubKey());
        header.setScriptSig(scriptSig);
        return block;
    }

}


package io.nuls.consensus.utils;

import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.DelegateAccountPo;
import io.nuls.db.entity.DelegatePo;
import io.nuls.db.entity.TransactionPo;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ConsensusBeanUtils {

    public static final BlockPo toPojo(Block block) {
        BlockPo po = new BlockPo();
        if(null!=block.getVersion()){
            po.setVarsion((int) block.getVersion().getVersion());
        }
        try {
            po.setBytes(block.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        po.setTxcount(block.getHeader().getTxCount());
        if (block.getHeader().getHeight() > 1) {
            po.setPreHash(block.getHeader().getPreHash().getDigestHex());
        }
        po.setMerkleHash(block.getHeader().getMerkleHash().getDigestHex());
        po.setHeight(block.getHeader().getHeight());
        po.setCreateTime(block.getHeader().getTime());
        po.setHash(block.getHeader().getHash().getDigestHex());
        po.setSign(block.getHeader().getSign().getSignBytes());
        po.setTxcount(block.getHeader().getTxCount());
        po.setConsensusAddress(block.getHeader().getPackingAddress());
        return po;
    }

    public static final Block fromPojo(BlockPo po) throws NulsException {
        if (null == po) {
            return null;
        }
        Block block = new Block();
        block.parse(po.getBytes());
        return block;
    }

    public static Consensus<Agent> fromPojo(DelegateAccountPo po) {
        if (null == po) {
            return null;
        }
        Agent agent = new Agent();
        agent.setStatus(ConsensusStatusEnum.WAITING.getCode());
        agent.setDeposit(Na.valueOf(po.getDeposit()));
        agent.setCommissionRate(PocConsensusConstant.DEFAULT_COMMISSION_RATE);
        agent.setDelegateAddress(po.getPeerAddress());
        agent.setIntroduction(po.getRemark());
        agent.setStartTime(po.getStartTime());
        Consensus<Agent> ca = new Consensus<>();
        ca.setAddress(po.getAddress());
        ca.setExtend(agent);
        return ca;
    }

    public static Consensus<Delegate> fromPojo(DelegatePo po) {
        if (null == po) {
            return null;
        }
        Consensus<Delegate> ca = new Consensus<>();
        ca.setAddress(po.getAddress());
        Delegate delegate = new Delegate();
        delegate.setDelegateAddress(po.getAgentAddress());
        delegate.setDeposit(Na.valueOf(po.getDeposit()));
        delegate.setStartTime(po.getTime());
        delegate.setId(po.getId());
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
        po.setPeerAddress(bean.getExtend().getDelegateAddress());
        po.setId(bean.getAddress());
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
        po.setId(bean.getExtend().getId());
        return po;
    }
}


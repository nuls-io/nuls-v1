package io.nuls.consensus.utils;

import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.NulsBlock;
import io.nuls.consensus.entity.genesis.DevGenesisBlock;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.Transaction;
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
        //todo 重新设计表结构
        BlockPo po = new BlockPo();
        po.setVarsion(0);
        try {
            po.setBytes(block.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        po.setTxcount(1);
        if (block.getHeader().getHeight() > 1) {
            po.setPreHash(block.getHeader().getPreHash().getDigestHex());
        }
        po.setMerkleHash(block.getHeader().getMerkleHash().getDigestHex());
        po.setHeight(block.getHeader().getHeight());
        po.setCreateTime(block.getHeader().getTime());
        po.setHash(block.getHeader().getHash().getDigestHex());
        po.setSign(block.getHeader().getSign().getSignBytes());
        po.setConsensusAddress("localhost");
        po.setPeriodStartTime(block.getHeader().getTime());
        po.setTimePeriod(1);
        return po;
    }

    public static final TransactionPo toPojo(Transaction tx) {
        TransactionPo po = new TransactionPo();
        //todo
        return po;
    }

    public static final Block fromPojo(BlockPo po) {
        if(null==po){
            return null;
        }
        NulsBlock block = new NulsBlock();
        //todo
        return DevGenesisBlock.getInstance();
    }

    public static final Transaction fromPojo(TransactionPo po) {
        //todo
        return null;
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


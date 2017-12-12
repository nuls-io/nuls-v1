package io.nuls.consensus.utils;

import io.nuls.account.entity.Address;
import io.nuls.consensus.constant.ConsensusRole;
import io.nuls.consensus.entity.ConsensusMember;
import io.nuls.consensus.entity.member.ConsensusMemberData;
import io.nuls.consensus.entity.member.ConsensusMemberImpl;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.ConsensusAccountPo;
import io.nuls.db.entity.TransactionPo;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ConsensusBeanUtils {

    public static final BlockPo toPojo(Block block) {
        BlockPo po = new BlockPo();
        //todo
        return po;
    }

    public static final TransactionPo toPojo(Transaction tx) {
        TransactionPo po = new TransactionPo();
        //todo
        return po;
    }

    public static final ConsensusAccountPo toPojo(ConsensusMemberImpl member) {
        ConsensusAccountPo po = new ConsensusAccountPo();
        po.setAddress(member.getAddress().toString());
        po.setAgentAddress(member.getExtend().getAgentAddress().toString());
        po.setDeposit(member.getExtend().getDeposit());
        po.setHash(member.getExtend().getId());
        po.setRole(member.getExtend().getRole().getCode());
//        po.setStartTime(member.getStartTime());
//        po.setStatus(member.getStatus());
        return po;
    }

    public static final ConsensusMemberImpl fromPojo(ConsensusAccountPo po) {
        ConsensusMemberImpl member = new ConsensusMemberImpl();
//        member.setHash(po.getHash());
//        member.setStatus(po.getStatus());
        member.setAddress(new Address(po.getAddress()));
        ConsensusMemberData data = new ConsensusMemberData();
        data.setRole(ConsensusRole.getConsensusRoleByCode(po.getRole()));
        data.setDeposit(po.getDeposit());
        data.setAgentAddress(new Address(po.getAgentAddress()));
//        member.setStartTime(po.getStartTime());
        member.setExtend(data);
        return member;
    }


    public static final Block fromPojo(BlockPo po) {
        //todo
        return null;
    }

    public static final Transaction fromPojo(TransactionPo po) {
        //todo
        return null;
    }


}

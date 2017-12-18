package io.nuls.consensus.utils;

import io.nuls.consensus.entity.NulsBlock;
import io.nuls.consensus.entity.genesis.DevGenesisBlock;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.BlockPo;
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
            po.setTxs(block.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        po.setTxcount(1);
        if(block.getHeader().getHeight()>1){
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
        //todo
        return DevGenesisBlock.getInstance();
    }

    public static final Transaction fromPojo(TransactionPo po) {
        //todo
        return null;
    }


}

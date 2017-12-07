package io.nuls.consensus.utils;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.TransactionPo;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ConsensusBeanUtils {

    public static final BlockPo toPojo(Block block){
        BlockPo po = new BlockPo();
        //todo
        return po;
    }

    public static final TransactionPo toPojo(Transaction tx){
        TransactionPo po = new TransactionPo();
        //todo
        return po;
    }
    public static final Block fromPojo(BlockPo po){
        //todo
        return null;
    }

    public static final Transaction fromPojo(TransactionPo po){
        //todo
        return null;
    }
}

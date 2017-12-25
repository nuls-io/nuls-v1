package io.nuls.db.util;

import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;

/**
 * @author vivi
 * @date 2017/12/23.
 */
public class TransactionPoTool {

    public static TransactionLocalPo toLocal(TransactionPo tx) {
        TransactionLocalPo localPo = new TransactionLocalPo();
        localPo.setHash(tx.getHash());
        localPo.setBlockHash(tx.getBlockHash());
        localPo.setBlockHeight(tx.getBlockHeight());
        localPo.setCreateTime(tx.getCreateTime());
        localPo.setType(tx.getType());
        localPo.setTxdata(tx.getTxdata());

        return localPo;
    }

    public static TransactionPo toTx(TransactionLocalPo localPo) {
        TransactionPo tx = new TransactionPo();
        tx.setHash(localPo.getHash());
        tx.setBlockHash(localPo.getBlockHash());
        tx.setBlockHeight(localPo.getBlockHeight());
        tx.setCreateTime(localPo.getCreateTime());
        tx.setType(localPo.getType());
        tx.setTxdata(localPo.getTxdata());

        return tx;
    }
}

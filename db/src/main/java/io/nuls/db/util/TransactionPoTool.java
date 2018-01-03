package io.nuls.db.util;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoOutputPo;

import java.io.IOException;

/**
 * @author vivi
 * @date 2017/12/23.
 */
public class TransactionPoTool {

    public static TransactionPo toPojo(Transaction tx) throws IOException {
        TransactionPo po = new TransactionPo();
        po.setHash(tx.getHash().getDigestHex());
        po.setType(tx.getType());
        po.setCreateTime(tx.getTime());
        po.setBlockHeight(tx.getBlockHeight());
        po.setBlockHash(tx.getBlockHash().getDigestHex());
        po.setTxdata(tx.serialize());
        return po;
    }

    public static TransactionLocalPo toPojoLocal(Transaction tx) throws IOException {
        TransactionLocalPo po = new TransactionLocalPo();
        po.setHash(tx.getHash().getDigestHex());
        po.setType(tx.getType());
        po.setCreateTime(tx.getTime());
        po.setBlockHeight(tx.getBlockHeight());
        po.setBlockHash(tx.getBlockHash().getDigestHex());
        po.setTxdata(tx.serialize());
        return po;
    }

    public static Transaction toTransaction(TransactionPo po) throws IllegalAccessException, NulsException, InstantiationException {
        Transaction tx = TransactionManager.getInstance(new NulsByteBuffer(po.getTxdata()));
        return tx;
    }
    public static Transaction toTransaction(TransactionLocalPo po) throws IllegalAccessException, NulsException, InstantiationException {
        Transaction tx = TransactionManager.getInstance(new NulsByteBuffer(po.getTxdata()));
        return tx;
    }

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

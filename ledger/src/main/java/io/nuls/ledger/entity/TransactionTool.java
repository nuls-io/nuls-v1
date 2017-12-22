package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.db.entity.TransactionPo;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author vivi
 * @date 2017/12/22.
 */
public class TransactionTool {

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

    public static Transaction toTransaction(TransactionPo po) throws IllegalAccessException, NulsException, InstantiationException {
        Transaction tx = TransactionManager.getInstance(new NulsByteBuffer(po.getTxdata()));
        return tx;
    }

}

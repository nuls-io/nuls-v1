package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.db.entity.TransactionPo;

import java.io.UnsupportedEncodingException;

/**
 * @author vivi
 * @date 2017/12/21.
 */
public class TransactionTool {

    public static TransactionPo toPojo(Transaction tx) throws UnsupportedEncodingException {
        TransactionPo po = new TransactionPo();
        po.setHash(tx.getHash().getDigestHex());
        po.setType(tx.getType());
        po.setRemark(new String(tx.getRemark(), NulsContext.DEFAULT_ENCODING));
        po.setCreateTime(tx.getTime());
//        po.setFee(tx.get);
        return po;
    }

    public static void toEntity(TransactionPo po , Transaction tx) {

    }
}

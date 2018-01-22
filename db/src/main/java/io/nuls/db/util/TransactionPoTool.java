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
package io.nuls.db.util;

import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;

import java.io.IOException;

/**
 * @author vivi
 * @date 2017/12/23.
 */
public class TransactionPoTool {

    public static TransactionPo toPojo(Transaction tx) throws IOException {
        TransactionPo po = new TransactionPo();
        if (tx.getHash() != null) {
            po.setHash(tx.getHash().getDigestHex());
        }
        po.setType(tx.getType());
        po.setCreateTime(tx.getTime());
        po.setBlockHeight(tx.getBlockHeight());
        po.setTxIndex(tx.getIndex());
        if (null != tx.getTxData()) {
            po.setTxData(tx.getTxData().serialize());
        }
        if (null != tx.getRemark()) {
            po.setRemark(new String(tx.getRemark(), NulsContext.DEFAULT_ENCODING));
        }
        if (null != tx.getFee()) {
            po.setFee(tx.getFee().getValue());
        }
        return po;
    }

    public static TransactionLocalPo toPojoLocal(Transaction tx) throws IOException {
        TransactionLocalPo po = new TransactionLocalPo();
        if (tx.getHash() != null) {
            po.setHash(tx.getHash().getDigestHex());
        }
        po.setType(tx.getType());
        po.setCreateTime(tx.getTime());
        po.setBlockHeight(tx.getBlockHeight());
        po.setTxIndex(tx.getIndex());

        if (null != tx.getTxData()) {
            po.setTxData(tx.getTxData().serialize());
        }
        if (null != tx.getRemark()) {
            po.setRemark(new String(tx.getRemark(), NulsContext.DEFAULT_ENCODING));
        }
        if (null != tx.getFee()) {
            po.setFee(tx.getFee().getValue());
        }

        return po;
    }

    public static Transaction toTransaction(TransactionPo po) throws Exception {
        Transaction tx = TransactionManager.getInstanceByType(po.getType());
        tx.setHash(new NulsDigestData(Hex.decode(po.getHash())));
        tx.setTime(po.getCreateTime());
        tx.setBlockHeight(po.getBlockHeight());
        tx.setFee(Na.valueOf(po.getFee()));
        tx.setIndex(po.getTxIndex());
        if(null!=po.getTxData()){
            tx.parseTxData(new NulsByteBuffer(po.getTxData()));
        }
        if(StringUtils.isNotBlank(po.getRemark())){
            tx.setRemark(po.getRemark().getBytes(NulsContext.DEFAULT_ENCODING));
        }
        return tx;
    }

    public static Transaction toTransaction(TransactionLocalPo po) throws Exception {
        Transaction tx = TransactionManager.getInstanceByType(po.getType());
        tx.setHash(new NulsDigestData(Hex.decode(po.getHash())));
        tx.setTime(po.getCreateTime());
        tx.setBlockHeight(po.getBlockHeight());
        tx.setFee(Na.valueOf(po.getFee()));
        tx.setIndex(po.getTxIndex());
        if(StringUtils.isNotBlank(po.getRemark())){
            tx.setRemark(po.getRemark().getBytes(NulsContext.DEFAULT_ENCODING));
        }
        if(null!=po.getTxData()){
            tx.parseTxData(new NulsByteBuffer(po.getTxData()));
        }
        return tx;
    }


    public static TransactionLocalPo toLocal(TransactionPo po) {
        TransactionLocalPo localPo = new TransactionLocalPo();
        localPo.setHash(po.getHash());
        localPo.setType(po.getType());
        localPo.setCreateTime(po.getCreateTime());
        localPo.setBlockHeight(po.getBlockHeight());
        localPo.setFee(po.getFee());
        localPo.setTxIndex(po.getTxIndex());
        localPo.setRemark(po.getRemark());
        localPo.setTxData(po.getTxData());
        return localPo;
    }

    public static TransactionPo toTx(TransactionLocalPo localPo) {
        TransactionPo po = new TransactionPo();
        po.setHash(localPo.getHash());
        po.setType(localPo.getType());
        po.setCreateTime(localPo.getCreateTime());
        po.setBlockHeight(localPo.getBlockHeight());
        po.setFee(localPo.getFee());
        po.setTxIndex(localPo.getTxIndex());
        po.setRemark(localPo.getRemark());
        po.setTxData(localPo.getTxData());

        return po;
    }

}

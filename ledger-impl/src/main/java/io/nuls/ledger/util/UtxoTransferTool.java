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
package io.nuls.ledger.util;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoInput;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.script.P2PKHScript;

import java.io.IOException;
import java.util.List;

public class UtxoTransferTool {

    public static UtxoOutput toOutput(UtxoOutputPo po) {
        UtxoOutput output = new UtxoOutput();
        output.setTxHash(new NulsDigestData(Hex.decode(po.getTxHash())));
        output.setIndex(po.getOutIndex());
        output.setLockTime(po.getLockTime());
        output.setValue(po.getValue());
        output.setAddress(new Address(po.getAddress()).getHash());
        try {
            output.setScript(new P2PKHScript(po.getScript()));
        }catch (Exception e){
            //todo
        }
        output.setStatus(po.getStatus());

        if(po.getCreateTime() != null) {
            output.setCreateTime(po.getCreateTime());
        }
        if(po.getTxType() != null) {
            output.setTxType(po.getTxType());
        }
        return output;
    }

    public static UtxoOutputPo toOutputPojo(UtxoOutput output) {
        UtxoOutputPo po = new UtxoOutputPo();
        po.setTxHash(output.getTxHash().getDigestHex());
        po.setOutIndex(output.getIndex());
        po.setValue(output.getValue());
        po.setLockTime(output.getLockTime());
        po.setAddress(Address.fromHashs(output.getAddress()).getBase58());
        if(null!=output.getScript()){
            po.setScript(output.getScript().getBytes());
        }
        po.setStatus((byte) output.getStatus());
        return po;
    }

    public static UtxoInput toInput(UtxoInputPo po) {
        UtxoInput input = new UtxoInput();
        input.setTxHash(new NulsDigestData(Hex.decode(po.getTxHash())));
        input.setIndex(po.getInIndex());
        input.setFromHash(new NulsDigestData(Hex.decode(po.getFromHash())));
        input.setFromIndex(po.getFromIndex());
        input.setScriptSig(po.getSign());

        UtxoOutput output = new UtxoOutput();
        output.setTxHash(new NulsDigestData(Hex.decode(po.getFromOutPut().getTxHash())));
        output.setIndex(po.getFromOutPut().getOutIndex());
        output.setLockTime(po.getFromOutPut().getLockTime());
        output.setValue(po.getFromOutPut().getValue());
        output.setAddress(new Address(po.getFromOutPut().getAddress()).getHash());
        input.setFrom(output);
        return input;
    }

    public static UtxoInputPo toInputPojo(UtxoInput input) {
        UtxoInputPo po = new UtxoInputPo();
        po.setTxHash(input.getTxHash().getDigestHex());
        po.setInIndex(input.getIndex());
        po.setFromHash(input.getFromHash().getDigestHex());
        po.setFromIndex(input.getFromIndex());
        po.setSign(input.getScriptSig());
        return po;
    }

    public static TransactionPo toTransactionPojo(Transaction tx) throws IOException {
        TransactionPo po = new TransactionPo();
        if (tx.getHash() != null) {
            po.setHash(tx.getHash().getDigestHex());
        }
        po.setType(tx.getType());
        po.setCreateTime(tx.getTime());
        po.setBlockHeight(tx.getBlockHeight());
        po.setTxIndex(tx.getIndex());

        if (null != tx.getSign()) {
            po.setSign(tx.getSign().serialize());
        }
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

    public static TransactionLocalPo toLocalTransactionPojo(Transaction tx) throws IOException {
        TransactionLocalPo po = new TransactionLocalPo();
        if (tx.getHash() != null) {
            po.setHash(tx.getHash().getDigestHex());
        }
        po.setType(tx.getType());
        po.setCreateTime(tx.getTime());
        po.setBlockHeight(tx.getBlockHeight());
        po.setTxIndex(tx.getIndex());
        po.setTransferType(tx.getTransferType());

        if (null != tx.getSign()) {
            po.setSign(tx.getSign().serialize());
        }
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

        if (po.getSign() != null) {
            tx.setSign(new NulsSignData(po.getSign()));
        }
        if (StringUtils.isNotBlank(po.getRemark())) {
            tx.setRemark(po.getRemark().getBytes(NulsContext.DEFAULT_ENCODING));
        }
        if (null != po.getTxData()) {
            tx.parseTxData(new NulsByteBuffer(po.getTxData()));
        }
        tx.setStatus(TxStatusEnum.CONFIRMED);
        transferCoinData(tx, po.getInputs(), po.getOutputs());
        return tx;
    }

    public static Transaction toTransaction(TransactionLocalPo po) throws Exception {
        Transaction tx = TransactionManager.getInstanceByType(po.getType());
        tx.setHash(new NulsDigestData(Hex.decode(po.getHash())));
        tx.setTime(po.getCreateTime());
        tx.setBlockHeight(po.getBlockHeight());
        tx.setFee(Na.valueOf(po.getFee()));
        tx.setIndex(po.getTxIndex());
        tx.setLocalTx(true);
        tx.setTransferType(po.getTransferType());

        if (po.getSign() != null) {
            tx.setSign(new NulsSignData(po.getSign()));
        }
        if (StringUtils.isNotBlank(po.getRemark())) {
            tx.setRemark(po.getRemark().getBytes(NulsContext.DEFAULT_ENCODING));
        }
        if (null != po.getTxData()) {
            tx.parseTxData(new NulsByteBuffer(po.getTxData()));
        }
        tx.setStatus(TxStatusEnum.CONFIRMED);
        transferCoinData(tx, po.getInputs(), po.getOutputs());
        return tx;
    }


    private static void transferCoinData(Transaction tx, List<UtxoInputPo> inputPoList, List<UtxoOutputPo> outputPoList) {
        if (tx instanceof AbstractCoinTransaction) {
            AbstractCoinTransaction coinTx = (AbstractCoinTransaction) tx;

            UtxoData utxoData = new UtxoData();

            for (UtxoInputPo inputPo : inputPoList) {
                utxoData.getInputs().add(toInput(inputPo));
            }

            for (UtxoOutputPo outputPo : outputPoList) {
                utxoData.getOutputs().add(toOutput(outputPo));
            }
            coinTx.setCoinData(utxoData);
        }
    }

}

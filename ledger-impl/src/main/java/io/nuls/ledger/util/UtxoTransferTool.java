/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.ledger.util;

import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.entity.OutPutStatusEnum;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoInput;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.constant.TxStatusEnum;
import io.nuls.protocol.model.Na;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.script.P2PKHScript;
import io.nuls.protocol.utils.TransactionManager;
import io.nuls.protocol.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Vive
 */
public class UtxoTransferTool {

    public static UtxoOutput toOutput(UtxoOutputPo po) {
        UtxoOutput output = new UtxoOutput();
        output.setTxHash(new NulsDigestData(Hex.decode(po.getTxHash())));
        output.setIndex(po.getOutIndex());
        output.setLockTime(po.getLockTime());
        output.setValue(po.getValue());
        output.setAddress(po.getAddress());
        try {
            output.setP2PKHScript(new P2PKHScript(po.getScript()));
        } catch (Exception e) {
            //todo
            Log.error(e);
        }
        if (po.getStatus() == UtxoOutputPo.USABLE) {
            if (po.isTimeLocked()) {
                output.setStatus(OutPutStatusEnum.UTXO_TIME_LOCK);
            } else {
                output.setStatus(OutPutStatusEnum.UTXO_UNSPENT);
            }
        } else if (po.getStatus() == UtxoOutputPo.LOCKED) {
            output.setStatus(OutPutStatusEnum.UTXO_CONSENSUS_LOCK);
        } else if (po.getStatus() == UtxoOutputPo.SPENT) {
            output.setStatus(OutPutStatusEnum.UTXO_SPENT);
        }

        if (po.getCreateTime() != null) {
            output.setCreateTime(po.getCreateTime());
        }
        if (po.getTxType() != null) {
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
        po.setAddress(output.getAddress());
        if (null != output.getP2PKHScript()) {
            po.setScript(output.getP2PKHScript().getBytes());
        }
        if (OutPutStatusEnum.UTXO_SPENT == output.getStatus()) {
            po.setStatus(UtxoOutputPo.SPENT);
        } else if (OutPutStatusEnum.UTXO_CONSENSUS_LOCK == output.getStatus()) {
            po.setStatus(UtxoOutputPo.LOCKED);
        } else {
            po.setStatus(UtxoOutputPo.USABLE);
        }
        return po;
    }

    public static UtxoInput toInput(UtxoInputPo po) {
        UtxoInput input = new UtxoInput();
        input.setTxHash(new NulsDigestData(Hex.decode(po.getTxHash())));
        input.setIndex(po.getInIndex());
        input.setFromHash(new NulsDigestData(Hex.decode(po.getFromHash())));
        input.setFromIndex(po.getFromIndex());

        UtxoOutput output = new UtxoOutput();
        output.setTxHash(new NulsDigestData(Hex.decode(po.getFromOutPut().getTxHash())));
        output.setIndex(po.getFromOutPut().getOutIndex());
        output.setLockTime(po.getFromOutPut().getLockTime());
        output.setValue(po.getFromOutPut().getValue());
        output.setAddress(po.getFromOutPut().getAddress());
        input.setFrom(output);
        return input;
    }

    public static UtxoInputPo toInputPojo(UtxoInput input) {
        UtxoInputPo po = new UtxoInputPo();
        po.setTxHash(input.getTxHash().getDigestHex());
        po.setInIndex(input.getIndex());
        po.setFromHash(input.getFromHash().getDigestHex());
        po.setFromIndex(input.getFromIndex());
        return po;
    }

    public static TransactionPo toTransactionPojo(Transaction tx, long blockHeight) throws IOException {
        TransactionPo po = new TransactionPo();
        if (tx.getHash() != null) {
            po.setHash(tx.getHash().getDigestHex());
        }
        po.setType(tx.getType());
        po.setCreateTime(tx.getTime());
        po.setBlockHeight(blockHeight);
        po.setTxIndex(tx.getIndex());
        po.setSize(tx.getSize());
        po.setScriptSig(tx.getScriptSig());
        if (null != tx.getTxData()) {
            po.setTxData(tx.getTxData().serialize());
        }
        if (null != tx.getRemark()) {
            po.setRemark(new String(tx.getRemark(), NulsConfig.DEFAULT_ENCODING));
        }
        if (null != tx.getFee()) {
            po.setFee(tx.getFee().getValue());
        }
        return po;
    }

    public static TransactionLocalPo toLocalTransactionPojo(Transaction tx) throws IOException {
        //TODO block height has thread sale problem
        if (tx == null || tx.getHash() == null) {
            return null;
        }
        TransactionLocalPo po = new TransactionLocalPo();
        po.setHash(tx.getHash().getDigestHex());
        po.setType(tx.getType());
        po.setCreateTime(tx.getTime());
        po.setBlockHeight(tx.getBlockHeight());
        po.setTxIndex(tx.getIndex());
        po.setTransferType(tx.getTransferType());
        po.setSize(tx.getSize());
        po.setScriptSig(tx.getScriptSig());
        if (null != tx.getTxData()) {
            po.setTxData(tx.getTxData().serialize());
        }
        if (null != tx.getRemark()) {
            po.setRemark(new String(tx.getRemark(), NulsConfig.DEFAULT_ENCODING));
        }
        if (null != tx.getFee()) {
            po.setFee(tx.getFee().getValue());
        }

        if (tx.getStatus() == TxStatusEnum.UNCONFIRM) {
            po.setTxStatus(TransactionLocalPo.UNCONFIRM);
        } else {
            po.setTxStatus(TransactionLocalPo.CONFIRM);
        }

        if (tx instanceof AbstractCoinTransaction) {
            po.setCoinData(((AbstractCoinTransaction) tx).getCoinData().serialize());
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
        tx.setSize(po.getSize());
        tx.setScriptSig(po.getScriptSig());
        if (StringUtils.isNotBlank(po.getRemark())) {
            tx.setRemark(po.getRemark().getBytes(NulsConfig.DEFAULT_ENCODING));
        }
        if (null != po.getTxData()) {
            tx.setTxData(tx.parseTxData(new NulsByteBuffer(po.getTxData())));
        }
        transferCoinData(tx, po.getInputs(), po.getOutputs());
        tx.setStatus(TxStatusEnum.CONFIRMED);
        return tx;
    }

    public static Transaction toTransaction(TransactionLocalPo po) throws NulsException {
        Transaction tx = TransactionManager.getInstanceByType(po.getType());
        tx.setHash(new NulsDigestData(Hex.decode(po.getHash())));
        tx.setTime(po.getCreateTime());
        tx.setBlockHeight(po.getBlockHeight());
        tx.setFee(Na.valueOf(po.getFee()));
        tx.setIndex(po.getTxIndex());
        tx.setTransferType(po.getTransferType());
        tx.setSize(po.getSize());
        tx.setScriptSig(po.getScriptSig());
        if (StringUtils.isNotBlank(po.getRemark())) {
            try {
                tx.setRemark(po.getRemark().getBytes(NulsConfig.DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException e) {
                throw new NulsException(e);
            }
        }
        if (null != po.getTxData() && po.getTxData().length > 0) {
            tx.setTxData(tx.parseTxData(new NulsByteBuffer(po.getTxData())));
        }
        if (po.getTxStatus() == TransactionLocalPo.UNCONFIRM) {
            tx.setStatus(TxStatusEnum.UNCONFIRM);
            AbstractCoinTransaction transaction = (AbstractCoinTransaction) tx;
            if (null != po.getCoinData() && po.getCoinData().length > 0) {
                transaction.parseCoinData(new NulsByteBuffer(po.getCoinData()));
            }
            if (po.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT ||
                    po.getType() == TransactionConstant.TX_TYPE_JOIN_CONSENSUS) {
                UtxoData utxoData = (UtxoData) transaction.getCoinData();
                UtxoOutput output = utxoData.getOutputs().get(0);
                output.setStatus(OutPutStatusEnum.UTXO_CONSENSUS_LOCK);
            }
            UtxoTransactionTool.getInstance().setTxhashToUtxo(tx);
        } else {
            tx.setStatus(TxStatusEnum.CONFIRMED);
            transferCoinData(tx, po.getInputs(), po.getOutputs());
        }

        return tx;
    }

    private static void transferCoinData(Transaction tx, List<UtxoInputPo> inputPoList, List<UtxoOutputPo> outputPoList) {
        if (tx instanceof AbstractCoinTransaction) {
            AbstractCoinTransaction coinTx = (AbstractCoinTransaction) tx;

            UtxoData utxoData = new UtxoData();
            Set<String> addressSet = new HashSet<>();

            for (UtxoInputPo inputPo : inputPoList) {
                utxoData.getInputs().add(toInput(inputPo));
                addressSet.add(inputPo.getFromOutPut().getAddress());
            }

            for (int i = 0; i < outputPoList.size(); i++) {
                UtxoOutputPo outputPo = outputPoList.get(i);
                utxoData.getOutputs().add(toOutput(outputPo));
            }
            coinTx.setCoinData(utxoData);
        }
    }
}

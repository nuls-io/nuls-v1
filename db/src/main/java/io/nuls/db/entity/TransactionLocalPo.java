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
package io.nuls.db.entity;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class TransactionLocalPo {

    private String hash;

    private Integer type;

    private Integer txIndex;

    private Long blockHeight;

    private Long createTime;

    private Long fee;

    private String remark;

    private Integer size;

    private int transferType;

    private byte[] txData;

    private byte[] coinData;

    private byte[] scriptSig;

    private List<UtxoInputPo> inputs;

    private List<UtxoOutputPo> outputs;

    public static final int UNCONFIRM = 0;
    public static final int CONFIRM = 1;
    private Integer txStatus;

    public TransactionLocalPo() {
    }

    public TransactionLocalPo(TransactionPo tx, int transferType) {
        this.hash = tx.getHash();
        this.type = tx.getType();
        this.txIndex = tx.getTxIndex();
        this.blockHeight = tx.getBlockHeight();
        this.createTime = tx.getCreateTime();
        this.fee = tx.getFee();
        this.transferType = transferType;
        this.remark = tx.getRemark();
        this.txData = tx.getTxData();
        this.scriptSig = tx.getScriptSig();
        this.size = tx.getSize();
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    public byte[] getTxData() {
        return txData;
    }

    public void setTxData(byte[] txData) {
        this.txData = txData;
    }

    public int getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(int txIndex) {
        this.txIndex = txIndex;
    }

    public byte[] getScriptSig() {
        return scriptSig;
    }

    public void setScriptSig(byte[] scriptSig) {
        this.scriptSig = scriptSig;
    }

    public int getTransferType() {
        return transferType;
    }

    public void setTransferType(int transferType) {
        this.transferType = transferType;
    }

    public List<UtxoInputPo> getInputs() {
        return inputs;
    }

    public void setInputs(List<UtxoInputPo> inputs) {
        this.inputs = inputs;
    }

    public List<UtxoOutputPo> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<UtxoOutputPo> outputs) {
        this.outputs = outputs;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }


    public byte[] getCoinData() {
        return coinData;
    }

    public void setCoinData(byte[] coinData) {
        this.coinData = coinData;
    }

    public Integer getTxStatus() {
        return txStatus;
    }

    public void setTxStatus(Integer txStatus) {
        this.txStatus = txStatus;
    }
}

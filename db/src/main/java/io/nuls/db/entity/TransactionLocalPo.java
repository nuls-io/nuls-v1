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
package io.nuls.db.entity;

import io.nuls.core.chain.entity.Transaction;

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

    private int transferType;

    private byte[] txData;

    private byte[] sign;

    private List<UtxoInputPo> inputs;

    private List<UtxoOutputPo> outputs;

    public TransactionLocalPo() {
    }

    public TransactionLocalPo(TransactionPo tx) {
        this.hash = tx.getHash();
        this.type = tx.getType();
        this.txIndex = tx.getTxIndex();
        this.blockHeight = tx.getBlockHeight();
        this.createTime = tx.getCreateTime();
        this.fee = tx.getFee();
        this.transferType = Transaction.TRANSFER_RECEIVE;
        this.remark = tx.getRemark();
        this.txData = tx.getTxData();
        this.sign = tx.getSign();
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

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
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
}

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
package io.nuls.contract.dto;

import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.AddressTool;

/**
 * @author: PierreLuo
 */
public class ContractTransfer {

    private byte[] from;

    private byte[] to;

    private Na value;

    private Na fee;

    private boolean isSendBack;

    /**
     *  智能合约交易hash
     */
    private NulsDigestData orginHash;

    /**
     *  合约转账(从合约转出)交易hash
     */
    private NulsDigestData hash;

    public ContractTransfer(){

    }

    public ContractTransfer(byte[] from, byte[] to, Na value, Na fee) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.fee = fee;
        this.isSendBack = false;
    }

    public ContractTransfer(byte[] from, byte[] to, Na value, Na fee, boolean isSendBack) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.fee = fee;
        this.isSendBack = isSendBack;
    }

    public byte[] getFrom() {
        return from;
    }

    public void setFrom(byte[] from) {
        this.from = from;
    }

    public byte[] getTo() {
        return to;
    }

    public void setTo(byte[] to) {
        this.to = to;
    }

    public Na getValue() {
        return value;
    }

    public void setValue(Na value) {
        this.value = value;
    }

    public Na getFee() {
        return fee;
    }

    public void setFee(Na fee) {
        this.fee = fee;
    }

    public boolean isSendBack() {
        return isSendBack;
    }

    public void setSendBack(boolean sendBack) {
        isSendBack = sendBack;
    }

    public NulsDigestData getOrginHash() {
        return orginHash;
    }

    public void setOrginHash(NulsDigestData orginHash) {
        this.orginHash = orginHash;
    }

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

}

/*
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
 *
 */
package io.nuls.kernel.model;

import io.nuls.kernel.script.P2PKHScriptSig;
import io.protostuff.Tag;

/**
 * @author vivi
 * @date 2017/10/30
 */
public class BlockHeader extends BaseNulsData {

    private transient NulsDigestData hash;
    @Tag(1)
    private NulsDigestData preHash;
    @Tag(2)
    private NulsDigestData merkleHash;
    @Tag(3)
    private long time;
    @Tag(4)
    private long height;
    @Tag(5)
    private long txCount;
    @Tag(6)
    private P2PKHScriptSig scriptSign;
    @Tag(7)
    private byte[] extend;

    private transient byte[] packingAddress;

    private transient int size;

    public BlockHeader() {
    }

    protected synchronized void calcHash() {
        if (null != this.hash) {
            return;
        }
        forceCalcHash();
    }

    protected void forceCalcHash() {
        P2PKHScriptSig tempSign = this.scriptSign;
        this.scriptSign = null;
        this.hash = NulsDigestData.calcDigestData(this.serialize());
        this.scriptSign = tempSign;
    }

    public NulsDigestData getHash() {
        if (null == hash) {
            calcHash();
        }
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public NulsDigestData getPreHash() {
        return preHash;
    }

    public void setPreHash(NulsDigestData preHash) {
        this.preHash = preHash;
    }

    public NulsDigestData getMerkleHash() {
        return merkleHash;
    }

    public void setMerkleHash(NulsDigestData merkleHash) {
        this.merkleHash = merkleHash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getTxCount() {
        return txCount;
    }

    public void setTxCount(long txCount) {
        this.txCount = txCount;
    }

    public P2PKHScriptSig getScriptSig() {
        return scriptSign;
    }

    public void setScriptSig(P2PKHScriptSig scriptSign) {
        this.scriptSign = scriptSign;
    }

    public byte[] getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

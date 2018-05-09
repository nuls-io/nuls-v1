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

package io.nuls.protocol.storage.po;

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.protostuff.Tag;

import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/5/8
 */
public class BlockHeaderPo extends BaseNulsData {

    private transient NulsDigestData hash;
    @Tag(1)
    private NulsDigestData preHash;
    @Tag(2)
    private NulsDigestData merkleHash;
    @Tag(3)
    private long time;
    @Tag(4)
    private long height = -1L;
    @Tag(5)
    private long txCount;
    @Tag(6)
    private byte[] packingAddress;
    @Tag(7)
    private P2PKHScriptSig scriptSign;
    @Tag(8)
    private byte[] extend;
    @Tag(9)
    private List<NulsDigestData> txHashList;

    public BlockHeaderPo() {
    }

    public BlockHeaderPo(Block block) {
        this.hash = block.getHeader().getHash();
        this.preHash = block.getHeader().getPreHash();
        this.merkleHash = block.getHeader().getPreHash();
        this.time = block.getHeader().getTime();
        this.height = block.getHeader().getHeight();
        this.txCount = block.getHeader().getTxCount();
        this.packingAddress = block.getHeader().getPackingAddress();
        this.scriptSign = block.getHeader().getScriptSig();
        this.extend = block.getHeader().getExtend();
        this.txHashList = block.getTxHashList();
    }

    public NulsDigestData getHash() {
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

    public byte[] getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
    }

    public P2PKHScriptSig getScriptSign() {
        return scriptSign;
    }

    public void setScriptSign(P2PKHScriptSig scriptSign) {
        this.scriptSign = scriptSign;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public List<NulsDigestData> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<NulsDigestData> txHashList) {
        this.txHashList = txHashList;
    }

    public BlockHeader toBlockHeader() {
        BlockHeader header = new BlockHeader();
        header.setHash(this.getHash());
        header.setHeight(this.getHeight());
        header.setExtend(this.getExtend());
        header.setPreHash(this.getPreHash());
        header.setTime(this.getTime());
        header.setMerkleHash(this.getMerkleHash());
        header.setTxCount(this.getTxCount());
        header.setScriptSig(this.getScriptSign());
        return header;
    }
}

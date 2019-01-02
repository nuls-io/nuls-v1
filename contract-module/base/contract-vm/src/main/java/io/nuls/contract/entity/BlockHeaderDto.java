/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.entity;


import io.nuls.contract.util.ContractUtil;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.model.BlockHeader;

import java.io.IOException;
import java.io.Serializable;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/5/2
 */
public class BlockHeaderDto implements Serializable {

    private String hash;
    private String preHash;
    private long time;
    private long height;
    private long txCount;
    //23 bytes
    private byte[] packingAddress;
    private byte[] stateRoot;

    public BlockHeaderDto() {}

    public BlockHeaderDto(BlockHeader header) {
        this.hash = (header.getHash() == null ? null : header.getHash().getDigestHex());
        this.preHash = (header.getPreHash() == null ? null : header.getPreHash().getDigestHex());
        this.time = header.getTime();
        this.height = header.getHeight();
        this.txCount = header.getTxCount();
        this.packingAddress = header.getPackingAddress();
        this.stateRoot = ContractUtil.getStateRoot(header);
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreHash() {
        return preHash;
    }

    public void setPreHash(String preHash) {
        this.preHash = preHash;
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

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }
}

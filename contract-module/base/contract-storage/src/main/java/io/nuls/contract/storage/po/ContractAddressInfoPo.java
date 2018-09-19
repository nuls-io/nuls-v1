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
package io.nuls.contract.storage.po;

import io.nuls.contract.util.ContractUtil;

import java.math.BigInteger;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/15
 */
public class ContractAddressInfoPo {

    private byte[] contractAddress;
    private byte[] sender;
    private byte[] createTxHash;
    private long createTime;
    private long blockHeight;
    private boolean acceptDirectTransfer;
    private boolean isNrc20;
    private String nrc20TokenName;
    private String nrc20TokenSymbol;
    private long decimals;
    private BigInteger totalSupply;

    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    public byte[] getSender() {
        return sender;
    }

    public void setSender(byte[] sender) {
        this.sender = sender;
    }

    public byte[] getCreateTxHash() {
        return createTxHash;
    }

    public void setCreateTxHash(byte[] createTxHash) {
        this.createTxHash = createTxHash;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public boolean isAcceptDirectTransfer() {
        return acceptDirectTransfer;
    }

    public void setAcceptDirectTransfer(boolean acceptDirectTransfer) {
        this.acceptDirectTransfer = acceptDirectTransfer;
    }

    public boolean isNrc20() {
        return isNrc20;
    }

    public void setNrc20(boolean nrc20) {
        isNrc20 = nrc20;
    }

    public String getNrc20TokenName() {
        return nrc20TokenName;
    }

    public void setNrc20TokenName(String nrc20TokenName) {
        this.nrc20TokenName = nrc20TokenName;
    }

    public String getNrc20TokenSymbol() {
        return nrc20TokenSymbol;
    }

    public void setNrc20TokenSymbol(String nrc20TokenSymbol) {
        this.nrc20TokenSymbol = nrc20TokenSymbol;
    }

    public long getDecimals() {
        return decimals;
    }

    public void setDecimals(long decimals) {
        this.decimals = decimals;
    }

    public BigInteger getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(BigInteger totalSupply) {
        this.totalSupply = totalSupply;
    }

    public boolean isLock() {
        return ContractUtil.isLockContract(this.blockHeight);
    }

    public int compareTo(long thatTime) {
        if(this.createTime > thatTime) {
            return -1;
        } else if(this.createTime < thatTime) {
            return 1;
        }
        return 0;
    }
}

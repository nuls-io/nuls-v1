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
 */
package io.nuls.consensus.poc.protocol.model;

import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.protocol.model.BaseNulsData;
import io.nuls.protocol.model.Na;
import io.nuls.protocol.utils.io.NulsByteBuffer;
import io.nuls.protocol.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/10
 */
public class Agent extends BaseNulsData {

    private Na deposit;

    private String packingAddress;

    private double commissionRate;

    private String introduction;

    private String agentName;

    private long startTime;

    private long blockHeight = -1L;

    private int status;
    private double creditVal;

    private long totalDeposit;
    private String txHash;

    @Override
    public int size() {
        int size = 0;
        size += Utils.sizeOfVarInt(deposit.getValue());
        size += Utils.sizeOfString(this.packingAddress);
        size += Utils.sizeOfDouble(this.commissionRate);
        size += Utils.sizeOfString(this.introduction);
        size += Utils.sizeOfString(agentName);
        size += Utils.sizeOfInt48();
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(deposit.getValue());
        stream.writeString(packingAddress);
        stream.writeDouble(this.commissionRate);
        stream.writeString(this.introduction);
        stream.writeString(agentName);
        stream.writeInt48(startTime);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.deposit = Na.valueOf(byteBuffer.readVarInt());
        this.packingAddress = byteBuffer.readString();
        this.commissionRate = byteBuffer.readDouble();
        this.introduction = byteBuffer.readString();
        this.agentName = byteBuffer.readString();
        this.startTime = byteBuffer.readInt48();
    }

    public Na getDeposit() {
        return deposit;
    }

    public void setDeposit(Na deposit) {
        this.deposit = deposit;
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = packingAddress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public void setCreditVal(double creditVal) {
        this.creditVal = creditVal;
    }

    public double getCreditVal() {
        return creditVal;
    }

    public long getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(long totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getTxHash() {
        return txHash;
    }
}

/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.consensus.poc.protocol.entity;

import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.TransactionLogicData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;
import io.protostuff.Tag;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/12/10
 */
public class Agent extends TransactionLogicData {


    private byte[] agentAddress;

    private byte[] packingAddress;

    private byte[] rewardAddress;

    private Na deposit;

    private double commissionRate;

    private byte[] agentName;

    private byte[] introduction;

    private transient long time;
    private transient long blockHeight = -1L;
    private transient long delHeight = -1L;
    private transient int status;
    private transient double creditVal;
    private transient long totalDeposit;
    private transient NulsDigestData txHash;

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfVarInt(deposit.getValue());
        size += this.packingAddress.length;
        size += SerializeUtils.sizeOfDouble(this.commissionRate);
        size += this.introduction.length;
        size += SerializeUtils.sizeOfBytes(agentName);
        size += SerializeUtils.sizeOfInt48();
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

    public byte[] getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(byte[] packingAddress) {
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

    public byte[] getIntroduction() {
        return introduction;
    }

    public void setIntroduction(byte[] introduction) {
        this.introduction = introduction;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public byte[] getAgentName() {
        return agentName;
    }

    public void setAgentName(byte[] agentName) {
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

    public void setTxHash(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    public NulsDigestData getTxHash() {
        return txHash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(long delHeight) {
        this.delHeight = delHeight;
    }

    public byte[] getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(byte[] agentAddress) {
        this.agentAddress = agentAddress;
    }

    public byte[] getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(byte[] rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    @Override
    public Agent clone() throws CloneNotSupportedException {
//        Agent agent = new Agent();
//
//        agent.setAgentAddress(getAgentAddress());
//        agent.setAgentName(getAgentName());
//        agent.setBlockHeight(getBlockHeight());
//        agent.setCommissionRate(getCommissionRate());
//        agent.setCreditVal(getCreditVal());
//        agent.setDelHeight(getDelHeight());
//        agent.setDeposit(getDeposit());
//        agent.setIntroduction(getIntroduction());
//        agent.setStatus(getStatus());
//        agent.setTime(getTime());
//        agent.setPackingAddress(getPackingAddress());
//        agent.setTotalDeposit(getTotalDeposit());
//        agent.setTxHash(getTxHash());
//
//        return agent;

        return (Agent) super.clone();
    }

    @Override
    public Set<byte[]> getAddresses() {
        Set<byte[]> addressSet = new HashSet<>();
        addressSet.add(this.agentAddress);
        return addressSet;
    }
}

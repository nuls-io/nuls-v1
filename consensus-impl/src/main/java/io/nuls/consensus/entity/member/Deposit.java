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
package io.nuls.consensus.entity.member;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class Deposit extends BaseNulsData {
    private Na deposit;
    private String agentHash;
    private int status;
    private long startTime;
    private String txHash;
    private long blockHeight = -1L;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Na getDeposit() {
        return deposit;
    }

    public void setDeposit(Na deposit) {
        this.deposit = deposit;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    @Override
    public int size() {
        int size = 0;
        size += Utils.sizeOfLong(deposit.getValue());
        size += Utils.sizeOfString(agentHash);
        size += Utils.sizeOfInt48();
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer buffer) throws IOException {
        buffer.writeVarInt(deposit.getValue());
        buffer.writeString(agentHash);
        buffer.writeInt48(startTime);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.deposit = Na.valueOf(byteBuffer.readVarInt());
        this.agentHash = byteBuffer.readString();
        this.startTime = byteBuffer.readInt48();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getTxHash() {
        return txHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }
}

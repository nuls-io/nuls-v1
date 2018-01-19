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
package io.nuls.consensus.entity.member;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class Delegate extends BaseNulsData {
    private Na deposit;
    private String delegateAddress;
    private int status;
    private long startTime;
    private String hash;

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

    public String getDelegateAddress() {
        return delegateAddress;
    }

    public void setDelegateAddress(String delegateAddress) {
        this.delegateAddress = delegateAddress;
    }

    @Override
    public int size() {
        int size = 0;
        size += VarInt.sizeOf(deposit.getValue());
        size += Utils.sizeOfSerialize(delegateAddress);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer buffer) throws IOException {
        buffer.writeVarInt(deposit.getValue());
        buffer.writeString(delegateAddress);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.deposit = Na.valueOf(byteBuffer.readVarInt());
        this.delegateAddress = byteBuffer.readString();
        try {
            this.hash = NulsDigestData.calcDigestData(this.serialize()).getDigestHex();
        } catch (IOException e) {
            Log.error(e);
        }
    }

    public String getHash() {
        if(hash==null){
            try {
                this.hash = NulsDigestData.calcDigestData(this.serialize()).getDigestHex();
            } catch (IOException e) {
                Log.error(e);
            }
        }
        return hash;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}

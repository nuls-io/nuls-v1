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

package io.nuls.sdk.model;

import io.nuls.sdk.exception.NulsException;
import io.nuls.sdk.utils.NulsByteBuffer;
import io.nuls.sdk.utils.NulsOutputStreamBuffer;
import io.nuls.sdk.utils.SerializeUtils;

import java.io.IOException;

/**
 *
 * @author ln
 */
public class Coin extends BaseNulsData {

    private byte[] owner;

    private Na na;

    private long lockTime;

    private transient Coin from;

    public Coin() {
    }

    public Coin(byte[] owner, Na na) {
        this.owner = owner;
        this.na = na;
    }

    public Coin(byte[] owner, Na na, long lockTime) {
        this(owner, na);
        this.lockTime = lockTime;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(owner);
        stream.writeInt64(na.getValue());
        stream.writeUint48(lockTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.owner = byteBuffer.readByLengthByte();
        this.na = Na.valueOf(byteBuffer.readInt64());
        this.lockTime = byteBuffer.readUint48();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfBytes(owner);
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfUint48();
        return size;
    }


    public Na getNa() {
        return na;
    }

    public byte[] getOwner() {
        return owner;
    }

    public long getLockTime() {
        return lockTime;
    }

    public Coin getFrom() {
        return from;
    }

    public void setFrom(Coin from) {
        this.from = from;
    }

    public void setOwner(byte[] owner) {
        this.owner = owner;
    }

    public void setNa(Na na) {
        this.na = na;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

}
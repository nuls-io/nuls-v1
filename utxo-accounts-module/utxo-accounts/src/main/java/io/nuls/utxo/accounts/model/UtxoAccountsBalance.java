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
package io.nuls.utxo.accounts.model;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;

public class UtxoAccountsBalance extends BaseNulsData {
    private byte[] owner;
    private Na balance;
    private Na hadLocked;

    public byte[] getOwner() {
        return owner;
    }

    public void setOwner(byte[] owner) {
        this.owner = owner;
    }

    public Na getBalance() {
        return balance;
    }

    public void setBalance(Na balance) {
        this.balance = balance;
    }

    public Na getHadLocked() {
        return hadLocked;
    }

    public void setHadLocked(Na hadLocked) {
        this.hadLocked = hadLocked;
    }

    @Override
    public int size() {
        int size=0;
        size += SerializeUtils.sizeOfBytes(owner);
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(owner);
        stream.writeInt64(balance.getValue());
        stream.writeInt64(hadLocked.getValue());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.owner = byteBuffer.readByLengthByte();
        this.balance = Na.valueOf(byteBuffer.readInt64());
        this.hadLocked = Na.valueOf(byteBuffer.readInt64());
    }
}

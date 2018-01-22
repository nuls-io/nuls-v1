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
package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.crypto.script.Script;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * Created by win10 on 2017/10/30.
 */
public class UtxoOutput extends BaseNulsData {

    private NulsDigestData txHash;

    private int index;

    private long value;

    private UtxoInput spentBy;

    private byte[] address;

    private long lockTime;

    private byte[] scriptBytes;

    private Script script;

    //0: useable, 1:locked， 2：spent
    private int status;

    public static final int USEABLE = 0;
    public static final int LOCKED = 1;
    public static final int SPENT = 2;

    private Transaction parent;

    public UtxoOutput() {

    }

    public UtxoOutput(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    @Override
    public int size() {
        int s = 0;
        s += Utils.sizeOfSerialize(txHash);
        s += VarInt.sizeOf(index);
        s += VarInt.sizeOf(value);
        s += Utils.sizeOfSerialize(address);
        s += VarInt.sizeOf(lockTime);
        s += Utils.sizeOfSerialize(scriptBytes);
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(txHash);
        stream.writeVarInt(index);
        stream.writeInt64(value);
        stream.writeBytesWithLength(address);
        stream.writeInt64(lockTime);
        stream.writeBytesWithLength(scriptBytes);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        if (byteBuffer == null) {
            return;
        }
        txHash = byteBuffer.readHash();
        index = (int) byteBuffer.readVarInt();
        value = byteBuffer.readInt64();
        address = byteBuffer.readByLengthByte();
        lockTime = byteBuffer.readInt64();
        scriptBytes = byteBuffer.readByLengthByte();
        script = new Script(scriptBytes);
    }


    public NulsDigestData getTxHash() {
        return txHash;
    }

    public void setTxHash(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    public UtxoInput getSpentBy() {
        return spentBy;
    }

    public void setSpentBy(UtxoInput spentBy) {
        this.spentBy = spentBy;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public byte[] getScriptBytes() {
        return scriptBytes;
    }

    public void setScriptBytes(byte[] scriptBytes) {
        this.scriptBytes = scriptBytes;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Transaction getParent() {
        return parent;
    }

    public void setParent(Transaction parent) {
        this.parent = parent;
    }
}

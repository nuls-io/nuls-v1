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
package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.script.P2PKHScript;
import io.nuls.core.script.Script;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;

/**
 * Created by win10 on 2017/10/30.
 */
public class UtxoOutput extends BaseNulsData implements Comparable<UtxoOutput> {

    private NulsDigestData txHash;

    private int index;

    private long value;

    private UtxoInput spentBy;

    private byte[] address;

    private long lockTime;

    private Script script;

    private int status;

    /**
     * ------ redundancy ------
     */
    private Transaction parent;

    private long createTime;

    private int txType;

    // key = txHash + "-" + index, a key that will not be serialized, only used for caching
    private String key;


    public static final int UTXO_CONFIRM_UNLOCK = 0;
    public static final int UTXO_CONFIRM_LOCK = 1;
    public static final int UTXO_SPENT = 2;
    public static final int UTXO_UNCONFIRM_UNLOCK = 3;
    public static final int UTXO_UNCONFIRM_LOCK = 4;

    public UtxoOutput() {
    }

    public UtxoOutput(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(index);
        s += 8;
        s += Utils.sizeOfBytes(address);
        s += Utils.sizeOfInt6();
        s += Utils.sizeOfNulsData(script);
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(index);
        stream.writeInt64(value);
        stream.writeBytesWithLength(address);
        stream.writeInt48(lockTime);
        stream.writeNulsData(script);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        if (byteBuffer == null) {
            return;
        }
        index = (int) byteBuffer.readVarInt();
        value = byteBuffer.readInt64();
        address = byteBuffer.readByLengthByte();
        lockTime = byteBuffer.readInt48();
        script = byteBuffer.readNulsData(new P2PKHScript());
    }


    public NulsDigestData getTxHash() {
        if (txHash == null && parent != null) {
            this.txHash = parent.getHash();
        }
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

    public String getKey() {
        if (StringUtils.isBlank(key)) {
            key = this.getTxHash().getDigestHex() + "-" + index;
        }
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public boolean isUsable() {
        return status == UTXO_CONFIRM_UNLOCK || status == UTXO_UNCONFIRM_UNLOCK;
    }

    public boolean isUnConfirm() {
        return status == UTXO_UNCONFIRM_UNLOCK || status == UTXO_UNCONFIRM_LOCK;
    }

    public boolean isLocked() {
        return status == UTXO_UNCONFIRM_LOCK || status == UTXO_CONFIRM_LOCK;
    }

    @Override
    public int compareTo(UtxoOutput o) {
        if (this.value < o.getValue()) {
            return -1;
        } else if (this.value > o.getValue()) {
            return 1;
        }
        return 0;
    }
}

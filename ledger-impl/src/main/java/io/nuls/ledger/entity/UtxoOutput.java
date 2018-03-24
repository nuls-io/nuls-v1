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

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.script.P2PKHScript;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by win10 on 2017/10/30.
 */
public class UtxoOutput extends BaseNulsData implements Comparable<UtxoOutput> {

    private NulsDigestData txHash;

    private int index;

    private long value;

    private String address;

    private long lockTime;

    private P2PKHScript p2PKHScript;

    private OutPutStatusEnum status;

    /**
     * ------ redundancy ------
     */
    private long createTime;

    private int txType;

    // key = txHash + "-" + index, a key that will not be serialized, only used for caching
    private String key;


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
        s += Utils.sizeOfInt48();
        s += Utils.sizeOfNulsData(p2PKHScript);
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(index);
        stream.writeInt64(value);
        stream.writeInt48(lockTime);
        stream.writeNulsData(p2PKHScript);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        if (byteBuffer == null) {
            return;
        }
        index = (int) byteBuffer.readVarInt();
        value = byteBuffer.readInt64();
        lockTime = byteBuffer.readInt48();
        p2PKHScript = byteBuffer.readNulsData(new P2PKHScript());

        Address addressObj = new Address(NulsContext.getInstance().getChainId(NulsContext.CHAIN_ID), this.getOwner());

        this.address = addressObj.toString();
    }


    public NulsDigestData getTxHash() {
        return txHash;
    }

    public void setTxHash(NulsDigestData txHash) {
        this.txHash = txHash;
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

    public P2PKHScript getP2PKHScript() {
        return p2PKHScript;
    }

    public void setP2PKHScript(P2PKHScript p2PKHScript) {
        this.p2PKHScript = p2PKHScript;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public OutPutStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OutPutStatusEnum status) {
        this.status = status;
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

    @Override
    public int compareTo(UtxoOutput o) {
        if (this.value < o.getValue()) {
            return -1;
        } else if (this.value > o.getValue()) {
            return 1;
        }
        return 0;
    }

    public boolean isUsable() {
        return OutPutStatusEnum.UTXO_CONFIRM_UNSPEND == status || OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND == status;
    }

    public boolean isSpend() {
        return OutPutStatusEnum.UTXO_CONFIRM_SPEND == status || OutPutStatusEnum.UTXO_UNCONFIRM_SPEND == status;
    }

    public boolean isLocked() {
        return OutPutStatusEnum.UTXO_CONFIRM_CONSENSUS_LOCK == status ||
                OutPutStatusEnum.UTXO_UNCONFIRM_CONSENSUS_LOCK == status ||
                OutPutStatusEnum.UTXO_CONFIRM_TIME_LOCK == status ||
                OutPutStatusEnum.UTXO_UNCONFIRM_TIME_LOCK == status;
    }

    public boolean isConfirm() {
        return OutPutStatusEnum.UTXO_CONFIRM_UNSPEND == status ||
                OutPutStatusEnum.UTXO_CONFIRM_TIME_LOCK == status ||
                OutPutStatusEnum.UTXO_CONFIRM_SPEND == status ||
                OutPutStatusEnum.UTXO_CONFIRM_CONSENSUS_LOCK == status;
    }

    public byte[] getOwner() {
        return this.getP2PKHScript().getPublicKeyDigest().getDigestBytes();
    }

}

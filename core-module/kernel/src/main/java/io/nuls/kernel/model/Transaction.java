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
 *
 */
package io.nuls.kernel.model;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.TxStatusEnum;
import io.nuls.kernel.func.TimeService;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Tag;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @author Niels
 * @date 2017/10/30
 */
public abstract class Transaction<T extends BaseNulsData> extends BaseNulsData implements Cloneable {

    @Tag(1)
    protected int type;
    @Tag(2)
    protected CoinData coinData;
    @Tag(3)
    protected T txData;
    @Tag(4)
    protected long time;
    @Tag(5)
    private byte[] scriptSig;
    @Tag(6)
    protected byte[] remark;

    protected transient NulsDigestData hash;

    protected transient int index;

    protected transient long blockHeight = -1L;

    protected transient TxStatusEnum status = TxStatusEnum.UNCONFIRM;

    public static final transient int TRANSFER_RECEIVE = 1;
    public static final transient int TRANSFER_SEND = 0;
    // when localTx is true, should care transferType
    protected transient int transferType;

    protected transient int size;

    protected transient boolean isMine;

    protected synchronized void calcHash() {
        if (null != hash) {
            return;
        }
        forceCalcHash();
    }

    protected void forceCalcHash() {
        this.hash = NulsDigestData.calcDigestData(this.serializeForHash());
    }


    public final byte[] serializeForHash() {
        RuntimeSchema schema = SCHEMA_MAP.get(this.getClass());
        Transaction tx = null;
        try {
            tx = (Transaction) this.clone();
        } catch (CloneNotSupportedException e) {
            Log.error(e);
        }
        tx.setScriptSig(null);
        return ProtostuffIOUtil.toByteArray(tx, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
    }

    public Transaction(int type) {
        this.dataType = NulsDataType.TRANSACTION;
        this.time = TimeService.currentTimeMillis();
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public byte[] getRemark() {
        return remark;
    }

    public void setRemark(byte[] remark) {
        this.remark = remark;
    }

    public NulsDigestData getHash() {
        if (null == hash) {
            this.calcHash();
        }
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public byte[] getScriptSig() {
        return scriptSig;
    }

    public void setScriptSig(byte[] scriptSig) {
        this.scriptSig = scriptSig;
    }

    public T getTxData() {
        return txData;
    }

    public void setTxData(T txData) {
        this.txData = txData;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public TxStatusEnum getStatus() {
        return status;
    }

    public void setStatus(TxStatusEnum status) {
        this.status = status;
    }

    public int getTransferType() {
        return transferType;
    }

    public void setTransferType(int transferType) {
        this.transferType = transferType;
    }

    public CoinData getCoinData() {
        return coinData;
    }

    public void setCoinData(CoinData coinData) {
        this.coinData = coinData;
    }

    public int getSize() {
        if (size == 0) {
            size = size();
        }
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

}

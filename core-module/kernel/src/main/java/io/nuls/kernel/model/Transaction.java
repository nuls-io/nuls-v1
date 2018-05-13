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

import io.nuls.core.tools.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.constant.TxStatusEnum;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.utils.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/10/30
 */
public abstract class Transaction<T extends TransactionLogicData> extends BaseNulsData implements Cloneable {

    protected int type;

    protected CoinData coinData;

    protected T txData;

    protected long time;

    private byte[] scriptSig;

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

    @Override
    public int size() {
        int size = 0;
        size += VarInt.sizeOf(type);
        size += VarInt.sizeOf(time);
        size += SerializeUtils.sizeOfBytes(remark);
        size += SerializeUtils.sizeOfNulsData(txData);
        size += SerializeUtils.sizeOfNulsData(coinData);
        size += SerializeUtils.sizeOfBytes(scriptSig);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(type);
        stream.writeVarInt(time);
        stream.writeBytesWithLength(remark);
        stream.writeNulsData(txData);
        stream.writeNulsData(coinData);
        stream.writeBytesWithLength(scriptSig);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        type = (int) byteBuffer.readVarInt();
        time = byteBuffer.readVarInt();
        this.remark = byteBuffer.readByLengthByte();
        txData = this.parseTxData(byteBuffer);
        try {
            hash = NulsDigestData.calcDigestData(this.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        this.coinData = byteBuffer.readNulsData(new CoinData());
        scriptSig = byteBuffer.readByLengthByte();
    }

    protected abstract T parseTxData(NulsByteBuffer byteBuffer) throws NulsException;

    public Transaction(int type) {
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

    public Na getFee() {
        Na fee = Na.ZERO;
        if (null != coinData) {
            fee = coinData.getFee();
        }
        return fee;
    }

    public byte[] getAddress() {
        return AddressTool.getAddress(scriptSig);
    }

    public List<byte[]> getAllRelativeAddress() {
        Set<byte[]> addresses = new HashSet<>();

        if (coinData != null) {
            Set<byte[]> coinAddressSet = coinData.getAddresses();
            addresses.addAll(coinAddressSet);
        }
        if (txData != null) {
            Set<byte[]> txAddressSet = txData.getAddresses();
            addresses.addAll(txAddressSet);
        }
        return new ArrayList<>(addresses);
    }

    public byte[] serializeForHash() throws IOException {
        ByteArrayOutputStream bos = null;
        try {
            int size = size();
            bos = new UnsafeByteArrayOutputStream(size);
            NulsOutputStreamBuffer buffer = new NulsOutputStreamBuffer(bos);
            if (size == 0) {
                bos.write(NulsConstant.PLACE_HOLDER);
            } else {
                buffer.writeVarInt(type);
                buffer.writeVarInt(time);
                buffer.writeBytesWithLength(remark);
                buffer.writeNulsData(txData);
                buffer.writeNulsData(coinData);
            }
            byte[] bytes = bos.toByteArray();
            if (bytes.length != this.size()) {
                throw new NulsRuntimeException(KernelErrorCode.FAILED, "date serialize for hash error：" + this.getClass());
            }
            return bytes;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
    }
}

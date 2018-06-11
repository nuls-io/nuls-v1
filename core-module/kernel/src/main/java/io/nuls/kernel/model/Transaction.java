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
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.constant.TxStatusEnum;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

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

    protected long blockHeight = -1L;

    protected transient TxStatusEnum status = TxStatusEnum.UNCONFIRM;

    protected transient int size;

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16(); // type
        size += SerializeUtils.sizeOfUint48(); // time
        size += SerializeUtils.sizeOfBytes(remark);
        size += SerializeUtils.sizeOfNulsData(txData);
        size += SerializeUtils.sizeOfNulsData(coinData);
        size += SerializeUtils.sizeOfBytes(scriptSig);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(type);
        stream.writeUint48(time);
        stream.writeBytesWithLength(remark);
        stream.writeNulsData(txData);
        stream.writeNulsData(coinData);
        stream.writeBytesWithLength(scriptSig);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        type = byteBuffer.readUint16();
        time = byteBuffer.readUint48();
        this.remark = byteBuffer.readByLengthByte();
        txData = this.parseTxData(byteBuffer);
        this.coinData = byteBuffer.readNulsData(new CoinData());
        try {
            hash = NulsDigestData.calcDigestData(this.serializeForHash());
        } catch (IOException e) {
            Log.error(e);
        }
        scriptSig = byteBuffer.readByLengthByte();
    }

    /**
     * 是否是系统产生的交易（打包节点产生，用于出块奖励结算、红黄牌惩罚），该种类型的交易在验证块大小时不计算在内，该类型交易不需要手续费
     * Is a system to produce trading (packaged node generation, for the piece reward settlement, CARDS punishment),
     * trading in the validation of this kind of new type block size is not taken into account, the types of transactions do not need poundage
     */
    public boolean isSystemTx() {
        return false;
    }

    /**
     * 是否是解锁交易，该类型交易会把锁定时间为-1的UTXO花费掉，生成新的UTXO
     * If it's an unlocking transaction, this type of transaction costs the UTXO with a lock time of -1 and generates a new UTXO
     */
    public boolean isUnlockTx() {
        return false;
    }

    /**
     * 该交易是否需要在账本中验证签名，所有系统产生的交易和一些特殊交易，不需要安装普通交易的方式验证签名，会提供额外的逻辑进行验证。
     * If the deal need to verify the signature in the book, all transactions system and some special deal,
     * no need to install the ordinary transaction way to verify the signature, will provide additional validation logic.
     */
    public boolean needVerifySignature() {
        return true;
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
        if (hash == null) {
            try {
                hash = NulsDigestData.calcDigestData(serializeForHash());
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public TxStatusEnum getStatus() {
        return status;
    }

    public void setStatus(TxStatusEnum status) {
        this.status = status;
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

    public Na getFee() {
        if (isSystemTx()) {
            return Na.ZERO;
        }
        Na fee = Na.ZERO;
        if (null != coinData) {
            fee = coinData.getFee();
        }
        return fee;
    }

    public byte[] getAddressFromSig() {
        return AddressTool.getAddress(scriptSig);
    }

    public List<byte[]> getAllRelativeAddress() {
        Set<byte[]> addresses = new HashSet<>();

        if (coinData != null) {
            Set<byte[]> coinAddressSet = coinData.getAddresses();
            if (null != coinAddressSet) {
                addresses.addAll(coinAddressSet);
            }
        }
        if (txData != null) {
            Set<byte[]> txAddressSet = txData.getAddresses();
            if (null != txAddressSet) {
                addresses.addAll(txAddressSet);
            }
        }
        if (scriptSig != null) {
            try {
                P2PKHScriptSig sig = P2PKHScriptSig.createFromBytes(scriptSig);
                byte[] address = AddressTool.getAddress(sig);

                boolean hasExist = false;
                for (byte[] as : addresses) {
                    if (Arrays.equals(as, address)) {
                        hasExist = true;
                        break;
                    }
                }

                if (!hasExist) {
                    addresses.add(address);
                }
            } catch (NulsException e) {
                Log.error(e);
            }
        }
        return new ArrayList<>(addresses);
    }

    public byte[] serializeForHash() throws IOException {
        ByteArrayOutputStream bos = null;
        try {
            int size = size() - SerializeUtils.sizeOfBytes(scriptSig);

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
            return bos.toByteArray();
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

    public abstract String getInfo(byte[] address);
}

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

package io.nuls.contract.storage.po;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2018/7/23
 */
public class TransactionInfoPo extends BaseNulsData {

    private NulsDigestData txHash;

    private long blockHeight;

    private long time;

    private byte[] addresses;

    private int txType;

    private byte status;

    public TransactionInfoPo() {

    }

    public TransactionInfoPo(Transaction tx) {
        if (tx == null) {
            return;
        }
        this.txHash = tx.getHash();
        this.blockHeight = tx.getBlockHeight();
        this.time = tx.getTime();
        List<byte[]> addressList = tx.getAllRelativeAddress();

        byte[] addresses = new byte[addressList.size() * Address.ADDRESS_LENGTH];
        for (int i = 0; i < addressList.size(); i++) {
            System.arraycopy(addressList.get(i), 0, addresses, Address.ADDRESS_LENGTH * i, Address.ADDRESS_LENGTH);
        }
        this.addresses = addresses;
        this.txType = tx.getType();
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(this.txHash);
        stream.writeUint32(blockHeight);
        stream.writeUint48(time);
        stream.writeBytesWithLength(addresses);
        stream.writeUint16(txType);
        stream.write(status);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.txHash = byteBuffer.readHash();
        this.blockHeight = byteBuffer.readUint32();
        this.time = byteBuffer.readUint48();
        this.addresses = byteBuffer.readByLengthByte();
        this.txType = byteBuffer.readUint16();
        this.status = byteBuffer.readByte();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(txHash);
        // blockHeight
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfBytes(addresses);
        // txType
        size += SerializeUtils.sizeOfUint16();
        size += 1;
        return size;
    }

    public NulsDigestData getTxHash() {
        return txHash;
    }

    public void setTxHash(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    public byte[] getAddresses() {
        return addresses;
    }

    public void setAddresses(byte[] addresses) {
        this.addresses = addresses;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public int compareTo(long thatTime) {
        if(this.time > thatTime) {
            return -1;
        } else if(this.time < thatTime) {
            return 1;
        }
        return 0;
    }
}

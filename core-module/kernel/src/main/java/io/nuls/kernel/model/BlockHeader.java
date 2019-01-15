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
package io.nuls.kernel.model;

import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.script.BlockSignature;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;

/**
 * @author vivi
 */
public class BlockHeader extends BaseNulsData {

    private transient NulsDigestData hash;
    private NulsDigestData preHash;
    private NulsDigestData merkleHash;
    private long time;
    private long height;
    private long txCount;
    private BlockSignature blockSignature;
    private byte[] extend;
    /**
     * pierre add 智能合约世界状态根
     */
    private transient byte[] stateRoot;

    private transient int size;
    private transient byte[] packingAddress;
    private transient String packingAddressStr;

    public BlockHeader() {
    }

    protected synchronized void calcHash() {
        if (null != this.hash) {
            return;
        }
        hash = forceCalcHash();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(preHash);
        size += SerializeUtils.sizeOfNulsData(merkleHash);
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfBytes(extend);
        size += SerializeUtils.sizeOfNulsData(blockSignature);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(preHash);
        stream.writeNulsData(merkleHash);
        stream.writeUint48(time);
        stream.writeUint32(height);
        stream.writeUint32(txCount);
        stream.writeBytesWithLength(extend);
        stream.writeNulsData(blockSignature);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.preHash = byteBuffer.readHash();
        this.merkleHash = byteBuffer.readHash();
        this.time = byteBuffer.readUint48();
        this.height = byteBuffer.readUint32();
        this.txCount = byteBuffer.readUint32();
        this.extend = byteBuffer.readByLengthByte();
        try {
            this.hash = NulsDigestData.calcDigestData(this.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        this.blockSignature = byteBuffer.readNulsData(new BlockSignature());
//        }
    }

    private NulsDigestData forceCalcHash() {
        try {
            BlockHeader header = (BlockHeader) this.clone();
            header.setBlockSignature(null);
            return NulsDigestData.calcDigestData(header.serialize());
        } catch (Exception e) {
            throw new NulsRuntimeException(e);
        }
    }

    public NulsDigestData getHash() {
        if (null == hash) {
            calcHash();
        }
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public NulsDigestData getPreHash() {
        return preHash;
    }

    public void setPreHash(NulsDigestData preHash) {
        this.preHash = preHash;
    }

    public NulsDigestData getMerkleHash() {
        return merkleHash;
    }

    public void setMerkleHash(NulsDigestData merkleHash) {
        this.merkleHash = merkleHash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getTxCount() {
        return txCount;
    }

    public void setTxCount(long txCount) {
        this.txCount = txCount;
    }

    public BlockSignature getBlockSignature() {
        return blockSignature;
    }

    public void setBlockSignature(BlockSignature scriptSign) {
        this.blockSignature = scriptSign;
    }

    public byte[] getPackingAddress() {
        if (null == packingAddress && this.blockSignature != null) {
            this.packingAddress = AddressTool.getAddress(blockSignature.getPublicKey());
        }
        return packingAddress;
    }

    public String getPackingAddressStr() {
        if (null == packingAddressStr) {
            this.packingAddressStr = AddressTool.getStringAddressByBytes(getPackingAddress());
        }
        return packingAddressStr;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }

    @Override
    public String toString() {
        return "BlockHeader{" +
                "hash=" + hash.getDigestHex() +
                ", preHash=" + preHash.getDigestHex() +
                ", merkleHash=" + merkleHash.getDigestHex() +
                ", time=" + time +
                ", height=" + height +
                ", txCount=" + txCount +
                ", blockSignature=" + blockSignature +
                //", extend=" + Arrays.toString(extend) +
                ", size=" + size +
                ", packingAddress=" + (packingAddress == null ? packingAddress : AddressTool.getStringAddressByBytes(packingAddress)) +
                '}';
    }
}

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
package io.nuls.protocol.model;

import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.protocol.script.P2PKHScriptSig;
import io.nuls.protocol.utils.BlockHeaderValidatorManager;
import io.nuls.protocol.utils.io.NulsByteBuffer;
import io.nuls.protocol.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.List;

/**
 * @author vivi
 * @date 2017/10/30
 */
public class BlockHeader extends BaseNulsData {

    private NulsDigestData hash;
    private NulsDigestData preHash;
    private NulsDigestData merkleHash;

    private long time;

    private long height;

    private long txCount;

    private byte[] packingAddress;

    private P2PKHScriptSig scriptSign;

    private byte[] extend;

    private int size;

    public BlockHeader() {
        initValidators();
    }

    private void initValidators() {
        List<NulsDataValidator> list = BlockHeaderValidatorManager.getValidators();
        for (NulsDataValidator validator : list) {
            this.registerValidator(validator);
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += Utils.sizeOfNulsData(preHash);
        size += Utils.sizeOfNulsData(merkleHash);
        size += Utils.sizeOfVarInt(time);
        size += Utils.sizeOfVarInt(height);
        size += Utils.sizeOfVarInt(txCount);
        size += Utils.sizeOfBytes(packingAddress);
        size += Utils.sizeOfBytes(extend);
        size += Utils.sizeOfNulsData(scriptSign);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(preHash);
        stream.writeNulsData(merkleHash);
        stream.writeVarInt(time);
        stream.writeVarInt(height);
        stream.writeVarInt(txCount);
        stream.writeBytesWithLength(packingAddress);
        stream.writeBytesWithLength(extend);
        stream.writeNulsData(scriptSign);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.preHash = byteBuffer.readHash();
        this.merkleHash = byteBuffer.readHash();
        this.time = byteBuffer.readVarInt();
        this.height = byteBuffer.readVarInt();
        this.txCount = byteBuffer.readVarInt();
        this.packingAddress = byteBuffer.readByLengthByte();
        this.extend = byteBuffer.readByLengthByte();
        try {
            this.hash = NulsDigestData.calcDigestData(this.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        this.scriptSign = byteBuffer.readNulsData(new P2PKHScriptSig());
    }


    public NulsDigestData getHash() {
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

    public P2PKHScriptSig getScriptSig() {
        return scriptSign;
    }

    public void setScriptSig(P2PKHScriptSig scriptSign) {
        this.scriptSign = scriptSign;
    }

    public byte[] getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
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
}

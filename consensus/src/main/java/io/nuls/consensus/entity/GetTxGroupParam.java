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
package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/18
 */
public class GetTxGroupParam extends BaseNulsData {

    private NulsDigestData blockHash;

    private List<NulsDigestData> txHashList = new ArrayList<>();

    public GetTxGroupParam() {

    }

    @Override
    public int size() {
        int size = 0;
        size += Utils.sizeOfNulsData(blockHash);
        size += VarInt.sizeOf(txHashList.size());
        size += this.getTxHashBytesLength();
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(blockHash);
        stream.writeVarInt(txHashList.size());
        for (NulsDigestData data : txHashList) {
            stream.writeNulsData(data);
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.blockHash = byteBuffer.readHash();
        long txCount = byteBuffer.readVarInt();
        this.txHashList = new ArrayList<>();
        for (int i = 0; i < txCount; i++) {
            this.txHashList.add(byteBuffer.readHash());
        }
    }

    private int getTxHashBytesLength() {
        int size = 0;
        for (NulsDigestData hash : txHashList) {
            size += Utils.sizeOfNulsData(hash);
        }
        return size;
    }

    public NulsDigestData getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(NulsDigestData blockHash) {
        this.blockHash = blockHash;
    }

    public List<NulsDigestData> getTxHashList() {
        return txHashList;
    }

    public void addHash(NulsDigestData hash) {
        this.txHashList.add(hash);
    }

    public void setTxHashList(List<NulsDigestData> txHashList) {
        this.txHashList = txHashList;
    }
}

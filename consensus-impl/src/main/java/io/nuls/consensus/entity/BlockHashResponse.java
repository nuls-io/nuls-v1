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
package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2018/1/15
 */
public class BlockHashResponse extends BaseNulsData {

    private List<Long> heightList = new ArrayList<>();

    private List<NulsDigestData> hashList = new ArrayList<>();

    @Override
    public int size() {
        int size = 0;
        size += Utils.sizeOfSerialize(heightList.size());
        for (Long height : heightList) {
            size += Utils.sizeOfSerialize(height);
        }
        size += Utils.sizeOfSerialize(hashList.size());
        for (NulsDigestData hash : hashList) {
            size += Utils.sizeOfSerialize(hash);
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(heightList.size());
        for (Long height : heightList) {
            stream.writeVarInt(height);
        }
        stream.writeVarInt(hashList.size());
        for (NulsDigestData hash : hashList) {
            stream.writeNulsData(hash);
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int heightListSize = byteBuffer.readInt32LE();
        if (heightListSize > 0) {
            this.heightList = new ArrayList<>();
            for (int i = 0; i < heightListSize; i++) {
                heightList.add(byteBuffer.readVarInt());
            }
        }
        int hashListSize = byteBuffer.readInt32LE();
        if (hashListSize <= 0) {
            return;
        }
        this.hashList = new ArrayList<>();
        for (int i = 0; i < hashListSize; i++) {
            hashList.add(byteBuffer.readHash());
        }
    }

    public List<Long> getHeightList() {
        return heightList;
    }

    public List<NulsDigestData> getHashList() {
        return hashList;
    }

    public NulsDigestData getHash() {
        try {
            return NulsDigestData.calcDigestData(this.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        return null;
    }

    public void put(long height, NulsDigestData hash) {
        heightList.add(height);
        hashList.add(hash);
    }

    public void setHeightList(List<Long> heightList) {
        this.heightList = heightList;
    }

    public void setHashList(List<NulsDigestData> hashList) {
        this.hashList = hashList;
    }

    public void merge(BlockHashResponse response) {
        long lastEnd = this.heightList.get(heightList.size() - 1);
        long nowStart = response.getHeightList().get(0);
        if (nowStart == lastEnd + 1) {
            this.heightList.addAll(response.getHeightList());
            this.hashList.addAll(response.getHashList());
        }
    }
}

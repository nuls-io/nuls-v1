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
package io.nuls.protocol.model;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 区块hash列表应答数据的封装
 * Block hash table reply data encapsulation.
 *
 * @author Niels
 * @date 2018/1/15
 */
public class BlockHashResponse extends BaseNulsData {
    /**
     * 请求消息的hash值
     * the digest data of the request message
     */

    private NulsDigestData requestMessageHash;
    /**
     * 返回的高度列表
     * Returns a list of heights.
     */

    private List<Long> heightList = new ArrayList<>();
    /**
     * 返回的hash列表
     * Returns a list of hashes.
     */

    private List<NulsDigestData> hashList = new ArrayList<>();


    /**
     * 返回的高度列表
     * Returns a list of heights.
     */
    public List<Long> getHeightList() {
        return heightList;
    }


    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(requestMessageHash);
        size += SerializeUtils.sizeOfVarInt(heightList.size());
        for (Long height : heightList) {
            size += SerializeUtils.sizeOfVarInt(height);
        }
        size += SerializeUtils.sizeOfVarInt(hashList.size());
        for (NulsDigestData hash : hashList) {
            size += SerializeUtils.sizeOfNulsData(hash);
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(requestMessageHash);
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
        this.requestMessageHash = byteBuffer.readHash();
        long heightListSize = byteBuffer.readVarInt();
        if (heightListSize > 0) {
            this.heightList = new ArrayList<>();
            for (int i = 0; i < heightListSize; i++) {
                heightList.add(byteBuffer.readVarInt());
            }
        }
        long hashListSize = byteBuffer.readVarInt();
        if (hashListSize <= 0) {
            return;
        }
        this.hashList = new ArrayList<>();
        for (int i = 0; i < hashListSize; i++) {
            hashList.add(byteBuffer.readHash());
        }
    }


    /**
     * 返回的hash列表
     * Returns a list of hashes.
     */
    public List<NulsDigestData> getHashList() {
        return hashList;
    }

    public NulsDigestData getHash() {
        try {
            return NulsDigestData.calcDigestData(this.serialize());
        } catch (IOException e) {
            Log.error(e);
            return null;
        }
    }

    public void put(long height, NulsDigestData hash) {
        heightList.add(height);
        hashList.add(hash);
    }

    public void putFront(long height, NulsDigestData hash) {
        heightList.add(0, height);
        hashList.add(0, hash);
    }

    public NulsDigestData getRequestMessageHash() {
        return requestMessageHash;
    }

    public void setRequestMessageHash(NulsDigestData requestMessageHash) {
        this.requestMessageHash = requestMessageHash;
    }

    public void merge(BlockHashResponse response) {
        long lastEnd = this.heightList.get(heightList.size() - 1);
        long nowStart = response.getHeightList().get(0);
        if (nowStart == lastEnd + 1) {
            this.heightList.addAll(response.getHeightList());
            this.hashList.addAll(response.getHashList());
        }
    }

    public NulsDigestData getBestHash() {
        if (null == hashList || hashList.isEmpty()) {
            return null;
        }
        return hashList.get(hashList.size() - 1);
    }

    public long getBestHeight() {
        if (null == heightList || heightList.isEmpty()) {
            return 0L;
        }
        return heightList.get(heightList.size() - 1);
    }
}

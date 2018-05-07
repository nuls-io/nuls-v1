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

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.protostuff.Tag;

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
    @Tag(1)
    private NulsDigestData requestMessageHash;
    /**
     * 返回的高度列表
     * Returns a list of heights.
     */
    @Tag(2)
    private List<Long> heightList = new ArrayList<>();
    /**
     * 返回的hash列表
     * Returns a list of hashes.
     */
    @Tag(3)
    private List<NulsDigestData> hashList = new ArrayList<>();


    /**
     * 返回的高度列表
     * Returns a list of heights.
     * @return
     */
    public List<Long> getHeightList() {
        return heightList;
    }

    /**
     * 返回的hash列表
     * Returns a list of hashes.
     * @return
     */
    public List<NulsDigestData> getHashList() {
        return hashList;
    }

    public NulsDigestData getHash() {
        return NulsDigestData.calcDigestData(this.serialize());
    }

    public void put(long height, NulsDigestData hash) {
        heightList.add(height);
        hashList.add(hash);
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

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

/**
 * 请求区块或者请求区块头的数据封装
 * Request block or request block header data encapsulation.
 *
 * @author Niels
 * @date 2017/12/18
 */
public class GetBlockDataParam extends BaseNulsData {
    /**
     * 起始摘要，当只请求一个时，就是目标摘要，当请求多个时，是第一个的前一个摘要
     * The initial hash, when only one request is requested,
     * is the target hash, and when multiple requests are requested, it is the previous of the first hash.
     */
    @Tag(1)
    private NulsDigestData startHash;
    /**
     * 请求的最后一个区块摘要
     * the last hash of request
     */
    @Tag(2)
    private NulsDigestData endHash;
    @Tag(3)
    private long start;
    @Tag(4)
    private long size;

    public GetBlockDataParam() {
    }

    /**
     * 起始摘要，当只请求一个时，就是目标摘要，当请求多个时，是第一个的前一个摘要
     * The initial hash, when only one request is requested,
     * is the target hash, and when multiple requests are requested, it is the previous of the first hash.
     * @return
     */
    public NulsDigestData getStartHash() {
        return startHash;
    }

    public void setStartHash(NulsDigestData startHash) {
        this.startHash = startHash;
    }

    /**
     * 请求的最后一个区块摘要
     * the last hash of request
     * @return 摘要对象
     */
    public NulsDigestData getEndHash() {
        return endHash;
    }

    public void setEndHash(NulsDigestData endHash) {
        this.endHash = endHash;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}

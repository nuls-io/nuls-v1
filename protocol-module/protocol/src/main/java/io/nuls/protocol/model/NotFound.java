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
import io.nuls.protocol.constant.NotFoundType;
import io.protostuff.Tag;

/**
 * @author: Niels Wang
 * @date: 2018/4/9
 */
public class NotFound extends BaseNulsData {
    /**
     * 数据类型 {@link NotFoundType}
     * data type
     */
    @Tag(1)
    private NotFoundType type;

    /**
     * 数据摘要
     * request hash
     */
    @Tag(2)
    private NulsDigestData hash;

    public NotFound() {
    }

    public NotFound(NotFoundType type, NulsDigestData hash) {
        this.type = type;
        this.hash = hash;
    }

    /**
     * 数据类型 {@link NotFoundType}
     * data type
     */
    public NotFoundType getType() {
        return type;
    }

    public void setType(NotFoundType type) {
        this.type = type;
    }

    /**
     * 数据摘要
     * request hash
     */
    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }
}

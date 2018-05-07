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
 * 请求交易列表的数据封装
 * Request transaction list data encapsulation.
 *
 * @author Niels
 * @date 2017/12/18
 */
public class GetTxGroupParam extends BaseNulsData {

    /**
     * 请求的交易摘要列表
     * the list of transaction digest data
     */
    @Tag(1)
    private List<NulsDigestData> txHashList = new ArrayList<>();

    public GetTxGroupParam() {
    }

    /**
     * 请求的交易摘要列表
     * the list of transaction digest data
     */
    public List<NulsDigestData> getTxHashList() {
        return txHashList;
    }

    /**
     * 添加一个交易摘要到请求列表中
     * add a tx hash to ask list
     */
    public void addHash(NulsDigestData hash) {
        this.txHashList.add(hash);
    }

    public void setTxHashList(List<NulsDigestData> txHashList) {
        this.txHashList = txHashList;
    }
}

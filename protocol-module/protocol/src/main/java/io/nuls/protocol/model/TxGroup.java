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
import io.nuls.kernel.model.Transaction;
import io.protostuff.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于返回对等节点请求的交易列表的封装
 * The encapsulation of the transaction list used to return a peer request.
 *
 * @author Niels
 * @date 2018/1/3
 */
public class TxGroup extends BaseNulsData {

    /**
     * 应答的交易列表
     * transaction list for response
     */
    @Tag(1)
    private NulsDigestData requestHash;
    @Tag(2)
    private List<Transaction> txList;

    /**
     * 交易整理到hashmap中
     * The transaction is sorted into a hashmap.
     */
    private transient Map<NulsDigestData, Transaction> txMap;

    /**
     * 交易整理到hashmap中
     * The transaction is sorted into a hashmap.
     */
    private synchronized void initTxMap() {
        if (null != txMap) {
            return;
        }
        this.txMap = new HashMap<>();
        for (Transaction tx : txList) {
            txMap.put(tx.getHash(), tx);
        }
    }

    /**
     * 应答的交易列表
     * transaction list for response
     */
    public List<Transaction> getTxList() {
        return txList;
    }

    public void setTxList(List<Transaction> txList) {
        this.txList = txList;
        initTxMap();
    }

    public Transaction getTx(String digestHex) {
        return txMap.get(digestHex);
    }

    /**
     * 交易整理的hashmap
     * The transaction is sorted into a hashmap.
     */
    public Map<NulsDigestData, Transaction> getTxMap() {
        if (null == txMap) {
            initTxMap();
        }
        return txMap;
    }

    public NulsDigestData getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(NulsDigestData requestHash) {
        this.requestHash = requestHash;
    }
}

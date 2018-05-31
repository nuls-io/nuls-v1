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

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.utils.VarInt;

import java.io.IOException;
import java.util.ArrayList;
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

    private NulsDigestData requestHash;

    private List<Transaction> txList;

    /**
     * 交易整理到hashmap中
     * The transaction is sorted into a hashmap.
     */
    private transient Map<NulsDigestData, Transaction> txMap;

    @Override
    public int size() {
        int size = 0;
        size += requestHash.size();
        size += VarInt.sizeOf(txList.size());
        size += this.getTxListLength();
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(requestHash);
        stream.writeVarInt(txList.size());
        for (Transaction data : txList) {
            stream.writeNulsData(data);
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        requestHash = byteBuffer.readHash();
        long txCount = byteBuffer.readVarInt();
        this.txList = new ArrayList<>();
        for (int i = 0; i < txCount; i++) {
            try {
                this.txList.add(byteBuffer.readTransaction());
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }
        initTxMap();
    }

    private int getTxListLength() {
        int size = 0;
        for (Transaction tx : txList) {
            size += SerializeUtils.sizeOfNulsData(tx);
        }
        return size;
    }

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

    public Transaction getTx(NulsDigestData hash) {
        return txMap.get(hash);
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

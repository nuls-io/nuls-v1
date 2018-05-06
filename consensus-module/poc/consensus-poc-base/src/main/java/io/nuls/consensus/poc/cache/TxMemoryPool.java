/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.cache;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ln on 2018/4/13.
 */
public final class TxMemoryPool {

    private final static String CACHE_NAME = "tx-memory-pool";

    private Map<NulsDigestData, Transaction> container;

    public TxMemoryPool() {
        container = new HashMap<>();
    }

    public boolean add(Transaction tx) {
        container.put(tx.getHash(), tx);
        return true;
    }

    /**
     * Get a transaction through hash, do not remove the memory pool after obtaining
     *
     * 通过hash获取某笔交易，获取之后不移除内存池
     * @param hash
     * @return Transaction
     */
    public Transaction get(NulsDigestData hash) {
        Transaction tx = container.get(hash);
        return tx;
    }

    /**
     * Get a transaction, removed from the memory pool after acquisition
     *
     * 获取一笔交易，获取之后从内存池中移除
     * @return Transaction
     */
    public Transaction getAndRemove(NulsDigestData hash) {
        Transaction tx = get(hash);
        if(tx != null) {
            remove(hash);
        }
        return tx;
    }

    public List<Transaction> getAll() {
        return new ArrayList<>(container.values());
    }

    public boolean remove(NulsDigestData hash) {
        container.remove(hash);
        return true;
    }

    public boolean exist(NulsDigestData hash) {
        return container.containsKey(hash);
    }

    public void clear() {
        try {
            container.clear();
        } finally {
        }
    }
}

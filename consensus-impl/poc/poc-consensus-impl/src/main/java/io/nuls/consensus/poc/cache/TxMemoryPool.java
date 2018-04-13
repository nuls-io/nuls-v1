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
 */

package io.nuls.consensus.poc.cache;

import io.nuls.cache.util.CacheMap;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.queue.service.impl.QueueService;

/**
 * Created by ln on 2018/4/13.
 */
public final class TxMemoryPool {

    private final static String CACHE_NAME = "tx-memory-pool";

    private CacheMap<String, Transaction> container;
    private QueueService<String> txHashQueue;

    private final static String CACHE_NAME_ISOLATED = "isolated-tx-memory-pool";
    private CacheMap<String, Transaction> isolatedContainer;
    private QueueService<String> isolatedTxHashQueue;

    public TxMemoryPool() {
        txHashQueue = new QueueService<String>();
        txHashQueue.createQueue(CACHE_NAME, (long) Integer.MAX_VALUE, false);
        container = new CacheMap<String, Transaction>(CACHE_NAME, 64);

        isolatedTxHashQueue = new QueueService<String>();
        isolatedTxHashQueue.createQueue(CACHE_NAME_ISOLATED, (long) Integer.MAX_VALUE, false);
        isolatedContainer = new CacheMap<String, Transaction>(CACHE_NAME_ISOLATED, 16);
    }

    public boolean add(Transaction tx, boolean isIsolated) {
        //check Repeatability
        String hash = tx.getHash().getDigestHex();
        if(isolatedContainer.containsKey(hash) || container.containsKey(hash)) {
            return false;
        }
        if(isIsolated) {
            isolatedContainer.put(hash, tx);
            isolatedTxHashQueue.offer(CACHE_NAME_ISOLATED, hash);
        } else {
            container.put(hash, tx);
            txHashQueue.offer(CACHE_NAME, hash);
        }
        return true;
    }

    public Transaction get(String hash) {
        Transaction tx = container.get(hash);
        if(tx == null) {
            tx = isolatedContainer.get(hash);
        }
        return tx;
    }

    public Transaction get() {
        //TODO

        return null;
    }

    public boolean remove(String hash) {
        if(container.containsKey(hash)) {
            container.remove(hash);
            txHashQueue.remove(CACHE_NAME, hash);
            return true;
        } else if(isolatedContainer.containsKey(hash)) {
            isolatedContainer.remove(hash);
            isolatedTxHashQueue.remove(CACHE_NAME_ISOLATED, hash);
            return true;
        }
        return false;
    }

    public boolean exist(String hash) {
        return container.containsKey(hash) || isolatedContainer.containsKey(hash);
    }

    public void clear() {
        txHashQueue.clear(CACHE_NAME);
        container.clear();

        isolatedTxHashQueue.clear(CACHE_NAME_ISOLATED);
        isolatedContainer.clear();
    }
}

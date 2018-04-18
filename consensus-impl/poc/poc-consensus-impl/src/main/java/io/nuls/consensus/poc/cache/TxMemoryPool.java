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
import io.nuls.consensus.poc.locker.Lockers;
import io.nuls.core.utils.queue.service.impl.QueueService;
import io.nuls.protocol.model.Transaction;

import java.util.List;

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
        Lockers.TX_MEMORY_LOCK.lock();

        try {
            //check Repeatability
            String hash = tx.getHash().getDigestHex();
            if (isolatedContainer.containsKey(hash) || container.containsKey(hash)) {
                return false;
            }
            if (isIsolated) {
                isolatedContainer.put(hash, tx);
                isolatedTxHashQueue.offer(CACHE_NAME_ISOLATED, hash);
            } else {
                container.put(hash, tx);
                txHashQueue.offer(CACHE_NAME, hash);
            }
            return true;
        } finally {
            Lockers.TX_MEMORY_LOCK.unlock();
        }
    }

    /**
     * Get a transaction through hash, do not remove the memory pool after obtaining
     *
     * 通过hash获取某笔交易，获取之后不移除内存池
     * @param hash
     * @return Transaction
     */
    public Transaction get(String hash) {
        Lockers.TX_MEMORY_LOCK.lock();

        try {
            Transaction tx = container.get(hash);
            if (tx == null) {
                tx = isolatedContainer.get(hash);
            }
            return tx;
        } finally {
            Lockers.TX_MEMORY_LOCK.unlock();
        }
    }

    /**
     * Get a transaction, the first transaction received, removed from the memory pool after acquisition
     *
     * 获取一笔交易，最先收到的交易，获取之后从内存池中移除
     * @return Transaction
     */
    public Transaction get() {
        Lockers.TX_MEMORY_LOCK.lock();

        Transaction tx = null;
        try {
            String hash = txHashQueue.poll(CACHE_NAME);
            if(hash != null) {
                tx = container.get(hash);
            } else {
                hash = isolatedTxHashQueue.poll(CACHE_NAME_ISOLATED);
                if (hash != null) {
                    tx = isolatedContainer.get(hash);
                }
            }
        } finally {
            Lockers.TX_MEMORY_LOCK.unlock();
        }

        if(tx != null) {
            remove(tx.getHash().getDigestHex());
        }

        return tx;
    }

    /**
     * Get a transaction, removed from the memory pool after acquisition
     *
     * 获取一笔交易，获取之后从内存池中移除
     * @return Transaction
     */
    public Transaction getAndRemove(String hash) {
        Transaction tx = get(hash);
        if(tx != null) {
            remove(hash);
        }
        return tx;
    }

    public List<Transaction> getAll() {
        return container.values();
    }

    public List<Transaction> getAllIsolated() {
        return isolatedContainer.values();
    }

    public boolean remove(String hash) {
        Lockers.TX_MEMORY_LOCK.lock();

        try {
            if (container.containsKey(hash)) {
                container.remove(hash);
                txHashQueue.remove(CACHE_NAME, hash);
                return true;
            } else if (isolatedContainer.containsKey(hash)) {
                isolatedContainer.remove(hash);
                isolatedTxHashQueue.remove(CACHE_NAME_ISOLATED, hash);
                return true;
            }
            return false;
        } finally {
            Lockers.TX_MEMORY_LOCK.unlock();
        }
    }

    public boolean exist(String hash) {
        return container.containsKey(hash) || isolatedContainer.containsKey(hash);
    }

    public void clear() {
        Lockers.TX_MEMORY_LOCK.lock();

        try {
            txHashQueue.clear(CACHE_NAME);
            container.clear();

            isolatedTxHashQueue.clear(CACHE_NAME_ISOLATED);
            isolatedContainer.clear();
        } finally {
            Lockers.TX_MEMORY_LOCK.unlock();
        }
    }
}

/*
 *
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

package io.nuls.consensus.cache.manager.tx;

import io.nuls.cache.util.CacheMap;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;

import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/4/15
 */
public enum TxCacheManager {
    TX_CACHE_MANAGER;

    private ConfirmingTxCacheManager confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();
    private ReceivedTxCacheManager receivedTxCacheManager = ReceivedTxCacheManager.getInstance();
    private OrphanTxCacheManager orphanTxCacheManager = OrphanTxCacheManager.getInstance();

    public Transaction getTx(NulsDigestData hash) {
        Transaction tx = receivedTxCacheManager.getTx(hash);
        if (null == tx) {
            tx = confirmingTxCacheManager.getTx(hash);
        }
        if (null == tx) {
            tx = orphanTxCacheManager.getTx(hash);
        }
        return tx;
    }

    public void putTxToOrphanCache(Transaction tx) {
        orphanTxCacheManager.putTx(tx);
    }

    public void putTxToReceivedCache(Transaction tx) {
        receivedTxCacheManager.putTx(tx);
    }

    public void putTxToConfirmingCache(Transaction tx) {
        confirmingTxCacheManager.putTx(tx);
    }

    public void removeTxFromReceivedCache(NulsDigestData hash) {
        receivedTxCacheManager.removeTx(hash);
    }

    public void removeTxFromConfirmingCache(NulsDigestData hash) {
        confirmingTxCacheManager.removeTx(hash);
    }

    public void removeTxFromOrphanCache(NulsDigestData hash) {
        orphanTxCacheManager.removeTx(hash);
    }


    public void removeTxesFromReceivedCache(List<NulsDigestData> txHashList) {
        receivedTxCacheManager.removeTx(txHashList);
    }

    public void removeTxesFromConfirmingCache(List<NulsDigestData> txHashList) {
        confirmingTxCacheManager.removeTxList(txHashList);
    }

    public void removeTxesFromOrphanCache(List<NulsDigestData> txHashList) {
        orphanTxCacheManager.removeTx(txHashList);
    }

    public List<Transaction> getTxListFromReceivedCache() {
        return receivedTxCacheManager.getTxList();
    }

    public List<Transaction> getTxListFromOrphanCache() {
        return orphanTxCacheManager.getTxList();
    }

    public CacheMap<String, Transaction> getReceivedCacheMap() {
        return receivedTxCacheManager.getTxCache();
    }

    public CacheMap<String, Transaction> getOrphanCacheMap() {
        return orphanTxCacheManager.getTxCache();
    }

    public CacheMap<String, Transaction> getConfirmingCacheMap() {
        return confirmingTxCacheManager.getTxCache();
    }

    public void init() {
        this.receivedTxCacheManager.init();
        this.confirmingTxCacheManager.init();
        this.orphanTxCacheManager.init();

    }

    public void clear() {
        this.receivedTxCacheManager.clear();
        this.confirmingTxCacheManager.clear();
        this.orphanTxCacheManager.clear();

    }

    public boolean isNotExistInReceivedCache(NulsDigestData hash) {
        return !receivedTxCacheManager.txExist(hash);
    }

    public boolean isNotExistInConfirmingCache(NulsDigestData hash) {
        return !confirmingTxCacheManager.txExist(hash);
    }

    public boolean isNotExistInOrphanCache(NulsDigestData hash) {
        return !orphanTxCacheManager.txExist(hash);
    }

    public boolean isExistInOrphanCache(NulsDigestData hash) {
        return orphanTxCacheManager.txExist(hash);
    }

    public boolean isExistInReceivedCache(NulsDigestData hash) {
        return receivedTxCacheManager.txExist(hash);
    }

    public boolean isExistInConfirmingCache(NulsDigestData hash) {
        return confirmingTxCacheManager.txExist(hash);
    }

    public void putTxListToConfirmingCache(List<Transaction> txs) {
        for (Transaction tx : txs) {
            putTxToConfirmingCache(tx);
        }
    }
}

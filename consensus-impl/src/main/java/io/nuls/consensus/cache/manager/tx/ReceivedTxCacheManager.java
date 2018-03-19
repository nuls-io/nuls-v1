/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.cache.manager.tx;

import io.nuls.cache.util.CacheMap;
import io.nuls.consensus.cache.manager.tx.listener.ReceivedTxCacheListener;
import io.nuls.consensus.constant.ConsensusCacheConstant;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;

import java.util.List;

/**
 * @author Niels
 * @date 2018/1/5
 */
public class ReceivedTxCacheManager {
    private static ReceivedTxCacheManager INSTANCE = new ReceivedTxCacheManager();
    private static final String CACHE_NAME = "Received-tx-cache";
    private CacheMap<String, Transaction> txCache;

    private ReceivedTxCacheManager() {

    }

    public static ReceivedTxCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        txCache = new CacheMap<>(CACHE_NAME, 64, ConsensusCacheConstant.LIVE_TIME, 0, new ReceivedTxCacheListener());
    }

    public boolean txExist(NulsDigestData hash) {
        if (txCache == null||hash==null) {
            return false;
        }
        return null != txCache.get(hash.getDigestHex());
    }

    public Transaction getTx(NulsDigestData txHash) {

        return txCache.get(txHash.getDigestHex());
    }

    public void removeTx(List<NulsDigestData> txHashList) {
        for (NulsDigestData hash : txHashList) {
            txCache.remove(hash.getDigestHex());
        }
    }

    public List<Transaction> getTxList() {
        return txCache.values();
    }

    public void clear() {
        txCache.clear();
    }

    public void putTx(Transaction tx) {
        txCache.put(tx.getHash().getDigestHex(), tx);
    }
}

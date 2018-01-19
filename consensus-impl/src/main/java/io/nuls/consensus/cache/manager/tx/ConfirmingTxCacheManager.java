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
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;

import java.util.List;

/**
 * @author Niels
 * @date 2018/1/5
 */
public class ConfirmingTxCacheManager {
    private static ConfirmingTxCacheManager INSTANCE = new ConfirmingTxCacheManager();
    private static final String CACHE_NAME = "Confirming-tx-cache";
    /**
     * 2 minutes alive
     */
    private static final int LIVE_TIME = 120;
    private CacheMap<String, Transaction> txCache;

    private ConfirmingTxCacheManager() {
    }

    public static ConfirmingTxCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        txCache = new CacheMap<>(CACHE_NAME,64, LIVE_TIME, 0);
    }

    public void putTxList(List<Transaction> txs) {
        for (Transaction tx : txs) {
            txCache.put(tx.getHash().getDigestHex(), tx);
        }
    }

    public void removeTxList(List<NulsDigestData> txHashList) {
        for (NulsDigestData hash : txHashList) {
            txCache.remove(hash.getDigestHex());
        }
    }

    public void clear() {
        txCache.clear();
    }

    public Transaction getTx(NulsDigestData hash) {
        if(null==hash){
            return null;
        }
        return txCache.get(hash.getDigestHex());
    }

    public void putTx(Transaction tx) {
        this.txCache.put(tx.getHash().getDigestHex(),tx);
    }
}

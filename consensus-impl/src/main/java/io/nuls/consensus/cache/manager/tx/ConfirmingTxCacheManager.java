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
        txCache = new CacheMap<>(CACHE_NAME, LIVE_TIME, 0);
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
}

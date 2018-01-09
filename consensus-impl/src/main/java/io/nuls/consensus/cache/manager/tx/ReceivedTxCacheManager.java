package io.nuls.consensus.cache.manager.tx;

import io.nuls.cache.util.CacheMap;
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
        txCache = new CacheMap<>(CACHE_NAME,64, ConsensusCacheConstant.LIVE_TIME, 0);
    }

    public boolean txExist(NulsDigestData hash) {
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

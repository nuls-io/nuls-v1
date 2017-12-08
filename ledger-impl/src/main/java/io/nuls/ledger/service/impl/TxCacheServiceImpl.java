package io.nuls.ledger.service.impl;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.ledger.service.intf.TxCacheService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class TxCacheServiceImpl implements TxCacheService {

    private static final String CACHE_TXS_TITLE = "transaction-cahce";

    private static TxCacheServiceImpl INSTANCE = new TxCacheServiceImpl();
    private CacheService<String, Transaction> cacheService = NulsContext.getInstance().getService(CacheService.class);

    private TxCacheServiceImpl() {
        cacheService.createCache(CACHE_TXS_TITLE);
    }

    public static TxCacheServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void putTx(Transaction transaction) {
        //todo
    }

    @Override
    public Transaction getTx(String hashHex) {
        //todo
        return null;
    }

    @Override
    public List<Transaction> getTxList() {
        //todo
        return null;
    }

    @Override
    public List<Transaction> getTxList(long startTime, long endTime) {
        //todo
        return null;
    }
}

package io.nuls.ledger.service.impl;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.Balance;

/**
 * Created by Niels on 2017/11/17.
 */
public class LedgerCacheService {
    private static LedgerCacheService instance = new LedgerCacheService();
    private CacheService<String, Balance> cacheService = NulsContext.getInstance().getService(CacheService.class);

    private LedgerCacheService() {
        cacheService.createCache(LedgerConstant.STANDING_BOOK);
    }

    public static LedgerCacheService getInstance() {
        return instance;
    }

    public void putBalance(String address, Balance balance) {
        if (null == balance || StringUtils.isBlank(address)) {
            return;
        }
        cacheService.putElement(LedgerConstant.STANDING_BOOK, address, balance);
    }

    public void clear() {
        this.cacheService.clearCache(LedgerConstant.STANDING_BOOK);
    }

    public void destroy() {
        this.cacheService.removeCache(LedgerConstant.STANDING_BOOK);
    }

    public Balance getBalance(String address) {
        return cacheService.getElementValue(LedgerConstant.STANDING_BOOK, address);
    }
}

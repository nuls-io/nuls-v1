package io.nuls.ledger.service.impl;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.TxBroadCastStatusEnum;
import io.nuls.ledger.entity.Balance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class LedgerCacheService {
    private static LedgerCacheService instance = new LedgerCacheService();
    private final CacheService<String, Balance> cacheService;


    private LedgerCacheService() {
        cacheService = NulsContext.getInstance().getService(CacheService.class);
        cacheService.createCache(LedgerConstant.STANDING_BOOK);
    }

    public static LedgerCacheService getInstance() {
        return instance;
    }

    public void clear() {
        this.cacheService.clearCache(LedgerConstant.STANDING_BOOK);
    }

    public void destroy() {
        this.cacheService.removeCache(LedgerConstant.STANDING_BOOK);
    }


    public void putBalance(String address, Balance balance) {
        if (null == balance || StringUtils.isBlank(address)) {
            return;
        }
        cacheService.putElement(LedgerConstant.STANDING_BOOK, address, balance);
    }


    public Balance getBalance(String address) {
        return cacheService.getElement(LedgerConstant.STANDING_BOOK, address);
    }

}

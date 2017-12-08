package io.nuls.ledger.service.impl;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.TxBroadCastStatusEnum;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.service.intf.LedgerCacheService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class LedgerCacheServiceImpl implements LedgerCacheService {
    private static LedgerCacheServiceImpl instance = new LedgerCacheServiceImpl();
    private final CacheService<String, Balance> cacheService;

    private LedgerCacheServiceImpl() {
        cacheService = NulsContext.getInstance().getService(CacheService.class);
        cacheService.createCache(LedgerConstant.STANDING_BOOK);
    }

    public static LedgerCacheServiceImpl getInstance() {
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

    @Override
    public Balance getBalance(String address) {
        return cacheService.getElementValue(LedgerConstant.STANDING_BOOK, address);
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

    @Override
    public void removeTx(String hashHex) {
        //todo
    }

    @Override
    public TxBroadCastStatusEnum getTxBroadCastStatus(String hashHex) {
        //todo
        return null;
    }
}

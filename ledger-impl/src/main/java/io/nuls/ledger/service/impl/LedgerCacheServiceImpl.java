package io.nuls.ledger.service.impl;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.TxBroadCastStatusEnum;
import io.nuls.ledger.entity.Balance;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class LedgerCacheServiceImpl {
    private static LedgerCacheServiceImpl instance = new LedgerCacheServiceImpl();
    private final CacheService<String, Balance> cacheService;

    private LedgerCacheServiceImpl() {
        cacheService = NulsContext.getInstance().getService(CacheService.class);
        cacheService.createCache(LedgerConstant.STANDING_BOOK);
        cacheService.createCache(TransactionConstant.TX_LIST);
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

     
    public Balance getBalance(String address) {
        return cacheService.getElementValue(LedgerConstant.STANDING_BOOK, address);
    }

    
    public void putTx(Transaction transaction) {
        //todo
    }

    public Transaction getTx(String hashHex) {
        //todo
        return null;
    }

    public List<Transaction> getTxList() {
        //todo
        return null;
    }

    public List<Transaction> getTxList(long startTime, long endTime) {
        //todo
        return null;
    }

    public void removeTx(String hashHex) {
        //todo
    }

    public TxBroadCastStatusEnum getTxBroadCastStatus(String hashHex) {
        //todo
        return null;
    }
}

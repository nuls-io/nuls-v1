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
    private final CacheService<String, Transaction> txCacheService;


    private LedgerCacheService() {
        cacheService = NulsContext.getInstance().getService(CacheService.class);
        cacheService.createCache(LedgerConstant.STANDING_BOOK);

        txCacheService = NulsContext.getInstance().getService(CacheService.class);
        txCacheService.createCache(TransactionConstant.TX_LIST, 5 * 60, 0);
    }

    public static LedgerCacheService getInstance() {
        return instance;
    }

    public void clear() {
        this.cacheService.clearCache(LedgerConstant.STANDING_BOOK);
        this.txCacheService.clearCache(TransactionConstant.TX_LIST);
    }

    public void destroy() {
        this.cacheService.removeCache(LedgerConstant.STANDING_BOOK);
        this.txCacheService.removeCache(TransactionConstant.TX_LIST);
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


    public void putTx(Transaction tx) {
        txCacheService.putElement(TransactionConstant.TX_LIST, tx.getHash().getDigestHex(), tx);
    }

    public Transaction getTx(String hashHex) {
        return txCacheService.getElement(TransactionConstant.TX_LIST, hashHex);
    }

    public List<Transaction> getTxList() {
        return txCacheService.getElementList(TransactionConstant.TX_LIST);
    }

    public List<Transaction> getTxList(long startTime, long endTime) {
        List<Transaction> txList = new ArrayList<>();

        List<Transaction> caches = getTxList();
        for (Transaction tx : caches) {
            if (startTime <= tx.getTime() && tx.getTime() <= endTime) {
                txList.add(tx);
            }
        }
        return txList;
    }

    public void removeTx(String hashHex) {
        txCacheService.removeElement(TransactionConstant.TX_LIST, hashHex);
    }

    public TxBroadCastStatusEnum getTxBroadCastStatus(String hashHex) {
        //todo
        return null;
    }
}

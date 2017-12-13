package io.nuls.account.service.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.Account;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/15
 */
public class AccountCacheService {
    private static final AccountCacheService INSTANCE = new AccountCacheService();

    private final CacheService<String, Account> cacheService;

    private AccountCacheService() {
        this.cacheService = NulsContext.getInstance().getService(CacheService.class);
        cacheService.createCache(AccountConstant.ACCOUNT_LIST_CACHE);
    }

    public static AccountCacheService getInstance() {
        return INSTANCE;
    }


    public void putAccount(Account account) {
        this.cacheService.putElement(AccountConstant.ACCOUNT_LIST_CACHE, account.getId(), account);
    }

    public Account getAccountById(String id) {
        return this.cacheService.getElementValue(AccountConstant.ACCOUNT_LIST_CACHE, id);
    }

    public Account getAccountByAddress(String address) {
        List<Account> list = this.getAccountList();
        for (Account account : list) {
            if (account.getAddress().toString().equalsIgnoreCase(address)) {
                return account;
            }
        }
        return null;
    }

    public List<Account> getAccountList() {
        return this.cacheService.getElementValueList(AccountConstant.ACCOUNT_LIST_CACHE);
    }

    public void clear() {
        this.cacheService.clearCache(AccountConstant.ACCOUNT_LIST_CACHE);
    }

    public void destroy() {
        this.cacheService.removeCache(AccountConstant.ACCOUNT_LIST_CACHE);
    }

    public void putAccountList(List<Account> list) {
        if (null != list) {
            for (Account account : list) {
                this.putAccount(account);
            }
        }
    }
}

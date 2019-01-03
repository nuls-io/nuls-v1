/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.account.service;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.model.Account;
import io.nuls.kernel.model.Address;
import io.nuls.cache.CacheMap;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.kernel.utils.AddressTool;

import java.util.List;
import java.util.Map;

/**
 * 账户缓存服务类
 * Account Cache Service
 *
 * @author: Charlie
 */
public class AccountCacheService {

    private static final AccountCacheService INSTANCE = new AccountCacheService();

    private CacheMap<String, Account> cacheMap;

    /**
     * 本地账户集合
     * Collection of local accounts
     */
    public Map<String, Account> localAccountMaps;

    private AccountCacheService() {
        this.cacheMap = new CacheMap<>(AccountConstant.ACCOUNT_LIST_CACHE, 32, String.class, Account.class);
    }


    public static AccountCacheService getInstance() {
        return INSTANCE;
    }

    /**
     * 缓存一个账户
     * Cache an account
     *
     * @param account Account to be cached
     */
    public void putAccount(Account account) {
        this.cacheMap.put(account.getAddress().getBase58(), account);
    }

    /**
     * 根据账户地址获取账户详细信息
     * Get accounts based on account address
     *
     * @param address Account to be operated
     */
    public Account getAccountByAddress(String address) {
        List<Account> list = this.getAccountList();
        for (Account account : list) {
            if (account.getAddress().toString().equalsIgnoreCase(address)) {
                return account;
            }
        }
        return null;
    }

    /**
     * 验证账户是否存在
     * Verify the existence of the account
     */
    public boolean contains(byte[] address) {
        return this.cacheMap.containsKey(AddressTool.getStringAddressByBytes(address));
    }

    /**
     * 获取所有账户
     * Get all accounts
     *
     * @return List<Account>
     */
    public List<Account> getAccountList() {
        return this.cacheMap.values();
    }

    public void removeAccount(Address address) {
        this.cacheMap.remove(address.getBase58());
    }

    public void removeAccount(byte[] address) {
        this.cacheMap.remove(AddressTool.getStringAddressByBytes(address));
    }


    public void clear() {
        if (null == cacheMap) {
            return;
        }
        this.cacheMap.clear();
    }

    public void destroy() {
        this.cacheMap.destroy();
    }

    /**
     * 缓存多个账户
     * Cache multiple accounts
     */
    public void putAccountList(List<Account> list) {
        if (null != list) {
            for (Account account : list) {
                this.putAccount(account);
            }
        }
    }
}

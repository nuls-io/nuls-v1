package io.nuls.account.manager;

import io.nuls.account.entity.Account;
import io.nuls.account.service.impl.AccountCacheService;
import io.nuls.account.service.impl.AccountServiceImpl;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.context.NulsContext;

import java.util.List;

/**
 * Created by Niels on 2017/11/15.
 */
public class AccountManager {

    private static final AccountManager instance = new AccountManager();

    public static String Locla_acount_id = null;
    private AccountCacheService cacheService = AccountCacheService.getInstance();
    private AccountService accountService;

    private AccountManager() {
    }

    public static AccountManager getInstance() {
        return instance;
    }

    public void init() {
        accountService = AccountServiceImpl.getInstance();
        //default local account
        List<Account> list = this.accountService.getLocalAccountList();
        if (null != list && !list.isEmpty()) {
            Locla_acount_id = list.get(0).getId();
        }
    }

    public void clearCache() {
        cacheService.clear();
    }

    public void destroy() {
        cacheService.destroy();
    }
}

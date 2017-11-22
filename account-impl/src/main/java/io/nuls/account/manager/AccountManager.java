package io.nuls.account.manager;

import io.nuls.account.entity.Account;
import io.nuls.account.service.impl.AccountCacheService;
import io.nuls.account.service.impl.AccountServiceImpl;
import io.nuls.account.service.intf.AccountService;

import java.util.List;

/**
 *
 * @author Niels
 * @date 2017/11/15
 */
public class AccountManager {

    private static final AccountManager INSTANCE = new AccountManager();

    public static String Locla_acount_id = null;
    private AccountCacheService cacheService = AccountCacheService.getInstance();
    private AccountService accountService;

    private AccountManager() {
    }

    public static AccountManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        accountService = AccountServiceImpl.getInstance();
        //default local account
        List<Account> list = this.accountService.getLocalAccountList();
        if (null != list && !list.isEmpty()) {
            Locla_acount_id = list.get(0).getId();
        }else{
            Account account = this.accountService.createAccount();
            Locla_acount_id = account.getId();
        }
    }

    public void clearCache() {
        cacheService.clear();
    }

    public void destroy() {
        cacheService.destroy();
    }
}

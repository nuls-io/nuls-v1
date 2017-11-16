package io.nuls.account.module.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.manager.AccountManager;
import io.nuls.account.module.intf.AccountModule;
import io.nuls.account.service.impl.AccountServiceImpl;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;

/**
 * Created by Niels on 2017/10/30.
 */
public class AccountModuleImpl extends AccountModule {

    private AccountManager manager = AccountManager.getInstance();

    @Override

    public void start() {
        manager.init();
        AccountService accountService = AccountServiceImpl.getInstance();
        this.registerService(accountService);
    }


    @Override
    public void shutdown() {
        manager.clearCache();
    }

    @Override
    public void destroy() {
        manager.destroy();
    }

    @Override
    public String getInfo() {
        return "account module is " + this.getStatus();
    }

    @Override
    public int getVersion() {
        return AccountConstant.ACCOUNT_MODULE_VERSION;
    }
}

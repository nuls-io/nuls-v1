package io.nuls.account.module.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.event.BaseAccountEvent;
import io.nuls.account.module.intf.AccountModule;
import io.nuls.account.service.impl.AccountServiceImpl;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.event.bus.processor.service.intf.ProcessorService;

/**
 * Created by Niels on 2017/10/30.
 * nuls.io
 */
public class   AccountModuleImpl extends AccountModule {

    private NulsContext context = NulsContext.getInstance();
    private CacheService cacheService;

    @Override

    public void start() {
        cacheService = context.getService(CacheService.class);
//        AssertUtil.canNotEmpty(cacheService, "Account module depend on Cache Module");
//        cacheService.createCache(AccountConstant.ACCOUNT_LIST_CACHE);
        //todo 查询本地账户数据放入缓存中
        AccountService accountService = AccountServiceImpl.getInstance();
        this.registerService(accountService);

    }


    @Override
    public void shutdown() {
        cacheService.clearCache(AccountConstant.ACCOUNT_LIST_CACHE);
    }

    @Override
    public void destroy() {
        cacheService.clearCache(AccountConstant.ACCOUNT_LIST_CACHE);
        Log.warn("account module is destroyed");
    }

    @Override
    public String getInfo() {
        return "account module is "+this.getStatus();
    }

    @Override
    public int getVersion() {
        return AccountConstant.ACCOUNT_MODULE_VERSION;
    }
}

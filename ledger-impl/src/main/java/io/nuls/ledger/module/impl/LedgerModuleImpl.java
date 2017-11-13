package io.nuls.ledger.module.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.module.LedgerModule;
import io.nuls.ledger.service.impl.LedgerServiceImpl;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * Created by Niels on 2017/11/7.
 * nuls.io
 */
public class LedgerModuleImpl extends LedgerModule {

    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private CacheService cacheService = NulsContext.getInstance().getService(CacheService.class);

    @Override
    public void start() {
        //load account
        //calc balance
        //put StandingBook to cache

        this.registerService(LedgerServiceImpl.getInstance());
        //register handler


    }

    @Override
    public void shutdown() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public int getVersion() {
        return LedgerConstant.LEDGER_MODULE_VERSION;
    }
}

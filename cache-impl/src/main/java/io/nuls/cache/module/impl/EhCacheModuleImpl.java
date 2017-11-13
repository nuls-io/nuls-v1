package io.nuls.cache.module.impl;

import io.nuls.cache.constant.EhCacheConstant;
import io.nuls.cache.manager.EhCacheManager;
import io.nuls.cache.module.CacheModule;
import io.nuls.cache.service.impl.EhCacheServiceImpl;
import io.nuls.cache.service.intf.CacheService;

/**
 * Created by Niels on 2017/10/27.
 * nuls.io
 */
public class EhCacheModuleImpl extends CacheModule {

    private EhCacheManager cacheManager = EhCacheManager.getInstance();

    @Override
    public void start() {
        cacheManager.init();
        this.registerService(new EhCacheServiceImpl());
    }

    @Override
    public void shutdown() {
        cacheManager.close();
    }

    @Override
    public void destroy() {
        cacheManager.close();
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public int getVersion() {
        return EhCacheConstant.CACHE_MODULE_VERSION;
    }


}

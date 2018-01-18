package io.nuls.cache.module.impl;

import io.nuls.cache.constant.EhCacheConstant;
import io.nuls.cache.manager.EhCacheManager;
import io.nuls.cache.module.AbstractCacheModule;
import io.nuls.cache.service.impl.EhCacheServiceImpl;
import io.nuls.cache.service.intf.CacheService;

/**
 *
 * @author Niels
 * @date 2017/10/27
 *
 */
public class EhCacheModuleBootstrap extends AbstractCacheModule {

    private EhCacheManager cacheManager = EhCacheManager.getInstance();

    @Override
    public void init() {
        cacheManager.init();
    }

    @Override
    public void start() {

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

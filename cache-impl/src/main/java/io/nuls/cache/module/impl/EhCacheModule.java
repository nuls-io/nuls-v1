package io.nuls.cache.module.impl;

import io.nuls.cache.manager.EhCacheManager;
import io.nuls.cache.module.CacheModule;

/**
 * Created by Niels on 2017/10/27.
 * nuls.io
 */
public class EhCacheModule extends CacheModule {

    private EhCacheManager cacheManager = EhCacheManager.getInstance();

    @Override
    public void start() {
        cacheManager.init();
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
    public String getVersion() {
        return null;
    }


}

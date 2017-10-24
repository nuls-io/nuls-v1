package io.nuls.cache.module;


import io.nuls.core.module.NulsModule;

/**
 * Created by Niels on 2017/10/18.
 * nuls.io
 */
public abstract class CacheModule extends NulsModule {
    public CacheModule() {
        super(CacheModule.class.getSimpleName());
    }
}

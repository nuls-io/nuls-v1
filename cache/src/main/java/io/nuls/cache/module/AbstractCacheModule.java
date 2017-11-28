package io.nuls.cache.module;


import io.nuls.core.module.BaseNulsModule;

/**
 * @author Niels
 * @date 2017/10/18
 *
 */
public abstract class AbstractCacheModule extends BaseNulsModule {
    public AbstractCacheModule() {
        super((short) 3,"cache");
    }
}

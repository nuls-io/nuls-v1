package io.nuls.cache;

import io.nuls.task.NulsModule;

/**
 * Created by Niels on 2017/10/18.
 * nuls.io
 */
public abstract class CacheModule extends NulsModule {
    public CacheModule() {
        super(CacheModule.class.getSimpleName());
    }
}

package io.nuls.cache.module;


import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseNulsModule;

/**
 * @author Niels
 * @date 2017/10/18
 *
 */
public abstract class AbstractCacheModule extends BaseNulsModule {
    public AbstractCacheModule() {
        super(NulsConstant.MODULE_ID_CACHE);
    }
}

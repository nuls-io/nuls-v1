package io.nuls.db.module;


import io.nuls.core.module.BaseNulsModule;

/**
 *
 * @author zhouwei
 * @date 2017/9/26
 */
public abstract class AbstractDBModule extends BaseNulsModule {

    protected AbstractDBModule() {
        super((short) 2,"database");
    }

}

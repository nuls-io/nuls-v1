package io.nuls.db.module;


import io.nuls.core.module.NulsModule;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public abstract class DBModule extends NulsModule {

    protected DBModule() {
        super(DBModule.class.getSimpleName());
    }

}

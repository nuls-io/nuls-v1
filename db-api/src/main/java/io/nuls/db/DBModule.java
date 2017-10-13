package io.nuls.db;

import io.nuls.task.ModuleStatus;
import io.nuls.task.NulsModule;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public abstract class DBModule extends NulsModule {

    protected DBModule() {
        super(DBModule.class.getSimpleName());
    }

}

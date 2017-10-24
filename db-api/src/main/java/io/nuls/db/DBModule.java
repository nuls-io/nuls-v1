package io.nuls.db;

import io.nuls.db.intf.IBlockStore;
import io.nuls.task.ModuleStatus;
import io.nuls.task.NulsModule;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public abstract class DBModule extends NulsModule {

    private IBlockStore iBlockStore;
    protected DBModule() {
        super(DBModule.class.getSimpleName());
    }

    public IBlockStore getiBlockStore() {
        return iBlockStore;
    }

    public void setiBlockStore(IBlockStore iBlockStore) {
        this.iBlockStore = iBlockStore;
    }
}

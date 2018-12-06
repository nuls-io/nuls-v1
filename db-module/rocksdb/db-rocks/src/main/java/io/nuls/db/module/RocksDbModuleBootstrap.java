package io.nuls.db.module;

import io.nuls.db.manager.RocksDBManager;
import io.nuls.kernel.module.BaseModuleBootstrap;

public class RocksDbModuleBootstrap extends BaseModuleBootstrap {
    public RocksDbModuleBootstrap(short moduleId) {
        super(moduleId);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        RocksDBManager.close();
    }

    @Override
    public void destroy() {
        RocksDBManager.close();
    }
    @Override
    public String getInfo() {
        return null;
    }
}

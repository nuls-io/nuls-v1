package io.nuls.network;

import io.nuls.core.tools.log.Log;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.module.service.ModuleService;
import io.nuls.network.module.impl.NettyNetworkModuleBootstrap;

public class TestNetwork {

    public static void main(String[] args) {
        Thread.currentThread().setName("Nuls");
        try {
            sysStart();
        } catch (Exception e) {
            Log.error(e);
            System.exit(1);
        }
    }

    private static void sysStart() throws Exception {
        do {
            MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
            mk.init();
            mk.start();

            LevelDbModuleBootstrap dbModuleBootstrap = new LevelDbModuleBootstrap();
            dbModuleBootstrap.init();
            dbModuleBootstrap.start();


        } while (false);
    }

    private static void initModules() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        ModuleService.getInstance().startModule("db","io.nuls.db.module.impl.MybatisDbModuleBootstrap");
    }
}

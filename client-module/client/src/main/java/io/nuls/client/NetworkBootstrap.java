package io.nuls.client;

import io.nuls.core.tools.log.Log;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.network.module.impl.NettyNetworkModuleBootstrap;

public class NetworkBootstrap {

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
        MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
        mk.init();
        mk.start();

        LevelDbModuleBootstrap dbModuleBootstrap = new LevelDbModuleBootstrap();
        dbModuleBootstrap.init();
        dbModuleBootstrap.start();

        NettyNetworkModuleBootstrap networkModuleBootstrap = new NettyNetworkModuleBootstrap();
        networkModuleBootstrap.init();
        networkModuleBootstrap.start();

    }


}

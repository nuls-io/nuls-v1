package io.nuls.network.module;

import io.nuls.network.module.base.BootstrapDBCacheTest;
import io.nuls.network.module.base.MicroKernelBootstrap1Test;
import io.nuls.network.module.base.MicroKernelBootstrap2Test;

/**
 * @author: Charlie
 * @date: 2018/4/25
 */
public class BootstrapTest {
    public static void main(String[] args) {
        BootstrapDBCacheTest.start(MicroKernelBootstrap1Test.class);
    }
}

class Bootstrap2Test{
    public static void main(String[] args) {
        BootstrapDBCacheTest.start(MicroKernelBootstrap2Test.class);
    }
}

package io.nuls.test.network;

import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.message.bus.module.MessageBusModuleBootstrap;
import io.nuls.network.module.impl.NettyNetworkModuleBootstrap;
import io.nuls.protocol.base.module.BaseProtocolsModuleBootstrap;
import org.junit.Before;
import org.junit.Test;

public class TestNetwork {

    @Before
    public void init() {
        try {
            MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
            mk.init();
            mk.start();

            LevelDbModuleBootstrap dbModuleBootstrap = new LevelDbModuleBootstrap();
            dbModuleBootstrap.init();
            dbModuleBootstrap.start();

            BaseProtocolsModuleBootstrap protocolsModuleBootstrap = new BaseProtocolsModuleBootstrap();
            protocolsModuleBootstrap.init();
            protocolsModuleBootstrap.start();

            MessageBusModuleBootstrap messageBusModuleBootstrap = new MessageBusModuleBootstrap();
            messageBusModuleBootstrap.init();
            messageBusModuleBootstrap.start();

            NettyNetworkModuleBootstrap networkModuleBootstrap = new NettyNetworkModuleBootstrap();
            networkModuleBootstrap.init();
            networkModuleBootstrap.start();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testNetworkModule() {
        while (true) {
            try {
                Thread.sleep(1000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

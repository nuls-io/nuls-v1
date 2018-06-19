/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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

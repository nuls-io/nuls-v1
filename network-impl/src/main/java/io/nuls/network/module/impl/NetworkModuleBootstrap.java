/**
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
 */
package io.nuls.network.module.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.network.NetworkContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.entity.*;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.service.NetworkService;
import io.nuls.network.service.impl.NetworkServiceImpl;
import io.nuls.network.service.impl.netty.NettyClient;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.manager.EventManager;

import java.io.IOException;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkModuleBootstrap extends AbstractNetworkModule {

    private NetworkService networkService;

    @Override
    public void init() {
        this.waitForDependencyInited(NulsConstant.MODULE_ID_DB, NulsConstant.MODULE_ID_CACHE);
        this.registerEvent();
        try {
            NetworkContext.setNetworkConfig(ConfigLoader.loadProperties(NetworkConstant.NETWORK_PROPERTIES));
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.IO_ERROR);
        }

        this.registerService(NetworkServiceImpl.class);
        networkService = NulsContext.getServiceBean(NetworkService.class);
    }

    private void registerEvent() {
        EventManager.putEvent(GetVersionEvent.class);
        EventManager.putEvent(VersionEvent.class);
        EventManager.putEvent(GetNodeEvent.class);
        EventManager.putEvent(NodeEvent.class);
        EventManager.putEvent(GetNodesIpEvent.class);
        EventManager.putEvent(NodesIpEvent.class);
        EventManager.putEvent(HandshakeEvent.class);
    }

    @Override
    public void start() {
        this.waitForDependencyRunning(NulsConstant.MODULE_ID_EVENT_BUS);
        networkService.init();
        networkService.start();
    }

    @Override
    public void shutdown() {
        networkService.shutdown();
        NettyClient.worker.shutdownGracefully();
    }

    @Override
    public void destroy() {
        shutdown();
    }

    @Override
    public String getInfo() {
        StringBuilder str = new StringBuilder();
        str.append("\nmoduleName:");
        str.append(getModuleName());
        str.append(",p2p module info:");
        str.append("here is info");
        return str.toString();
    }
}

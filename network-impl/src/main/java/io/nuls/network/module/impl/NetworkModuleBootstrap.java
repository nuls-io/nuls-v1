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
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.network.NetworkContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.entity.GetNodeEvent;
import io.nuls.network.message.entity.GetVersionEvent;
import io.nuls.network.message.entity.NodeEvent;
import io.nuls.network.message.entity.VersionEvent;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.service.NetworkService;
import io.nuls.network.service.impl.NetworkServiceImpl;

import java.io.IOException;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkModuleBootstrap extends AbstractNetworkModule {

    private NetworkService networkService;

    @Override
    public void init() {
        try {
            NetworkContext.setNetworkConfig(ConfigLoader.loadProperties(NetworkConstant.NETWORK_PROPERTIES));
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.IO_ERROR);
        }
        this.registerService(NetworkServiceImpl.class);
        networkService = NulsContext.getServiceBean(NetworkService.class);
        networkService.init();
        this.registerEvent();

    }

    private void registerEvent() {
        EventManager.putEvent(GetVersionEvent.class);
        EventManager.putEvent(VersionEvent.class);
        EventManager.putEvent(GetNodeEvent.class);
        EventManager.putEvent(NodeEvent.class);
    }

    @Override
    public void start() {
        networkService.start();
    }

    @Override
    public void shutdown() {
        networkService.shutdown();
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

    @Override
    public int getVersion() {
        return 0;
    }


}

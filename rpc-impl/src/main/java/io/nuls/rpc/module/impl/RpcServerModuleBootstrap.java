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
package io.nuls.rpc.module.impl;

import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.constant.RpcConstant;
import io.nuls.rpc.module.AbstractRpcServerModule;
import io.nuls.rpc.service.impl.RpcServerServiceImpl;
import io.nuls.rpc.service.intf.RpcServerService;

/**
 *
 * @author Niels
 * @date 2017/9/27
 */
public class RpcServerModuleBootstrap extends AbstractRpcServerModule {

    private RpcServerService rpcServerService = RpcServerServiceImpl.getInstance();
    private String ip;
    private String port;
    private String moduleUrl;

    public RpcServerModuleBootstrap() {
        super();
    }

    @Override
    public void init() {
        this.ip = getModuleCfgProperty(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_IP);
        this.port = getModuleCfgProperty(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_PORT);
        this.moduleUrl = getModuleCfgProperty(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_URL);
    }

    @Override
    public void start() {
        this.registerService(rpcServerService);
        if (StringUtils.isBlank(ip) || StringUtils.isBlank(port)) {
            rpcServerService.startServer(RpcConstant.DEFAULT_IP, RpcConstant.DEFAULT_PORT, RpcConstant.DEFAULT_URL);
        } else {
            rpcServerService.startServer(ip, Integer.parseInt(port), moduleUrl);
        }
    }

    @Override
    public void shutdown() {
        rpcServerService.shutdown();
    }

    @Override
    public void destroy() {
        shutdown();
    }

    @Override
    public String getInfo() {
        StringBuilder str = new StringBuilder();
        str.append("moduleName:");
        str.append(getModuleName());
        str.append(",isServerRunning:");
        str.append(rpcServerService.isStarted());
        return str.toString();
    }

    @Override
    public int getVersion() {
        return RpcConstant.RPC_MODULE_VERSION;
    }

}

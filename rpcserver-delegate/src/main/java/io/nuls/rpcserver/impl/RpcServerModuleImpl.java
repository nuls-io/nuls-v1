package io.nuls.rpcserver.impl;

import io.nuls.rpcserver.intf.RpcServerModule;
import io.nuls.rpcserver.intf.RpcServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
@Service("rpcServerModule")
public class RpcServerModuleImpl extends RpcServerModule{

    @Autowired
    private RpcServerService rpcServerService;
    @Override
    public void start() {
        rpcServerService.init();
    }

    @Override
    public void shutdown() {
        rpcServerService.shutdown();
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
}

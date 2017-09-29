package io.nuls.rpcserver.impl;

import io.nuls.rpcserver.intf.RpcServerModule;
import io.nuls.rpcserver.intf.RpcServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
@Service("rpcServerModule")
public class RpcServerModuleImpl extends RpcServerModule {

    @Autowired
    private RpcServerService rpcServerService;
    private String ip;
    private String port;
    private String moduleUrl;

    @Override
    public void init(Map<String, String> initParams) {
        if(null==initParams){
            return;
        }
        this.ip = initParams.get("ip");
        this.port = initParams.get("port");
        this.moduleUrl = initParams.get("moduleUrl");
    }

    @Override
    public void start() {
        if(StringUtils.isEmpty(ip)||StringUtils.isEmpty(port)){
            rpcServerService.startServer();
        }else{
            rpcServerService.startServer(ip,Integer.parseInt(port),moduleUrl);
        }
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

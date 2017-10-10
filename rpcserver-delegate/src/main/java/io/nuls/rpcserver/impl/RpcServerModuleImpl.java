package io.nuls.rpcserver.impl;

import io.nuls.rpcserver.constant.RpcConstant;
import io.nuls.rpcserver.impl.services.RpcServerServiceImpl;
import io.nuls.rpcserver.intf.IRpcServerService;
import io.nuls.rpcserver.intf.RpcServerModule;
import io.nuls.task.ModuleStatus;
import io.nuls.util.str.StringUtils;

import java.util.Map;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
public class RpcServerModuleImpl extends RpcServerModule {

    private IRpcServerService rpcServerService = RpcServerServiceImpl.getInstance();
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
        setStatus(ModuleStatus.INITED);
    }

    @Override
    public void start() {
        if(getStatus()!=ModuleStatus.INITED){
            //TODO
        }
        setStatus(ModuleStatus.STARTING);
        if(StringUtils.isBlank(ip)|| StringUtils.isBlank(port)){
            rpcServerService.startServer(RpcConstant.DEFAULT_IP,RpcConstant.DEFAULT_PORT,RpcConstant.DEFAULT_URL);
        }else{
            rpcServerService.startServer(ip,Integer.parseInt(port),moduleUrl);
        }
        setStatus(ModuleStatus.RUNNING);
    }

    @Override
    public void shutdown() {
        if(getStatus()!=ModuleStatus.RUNNING){
            return;
        }
        rpcServerService.shutdown();
        setStatus(ModuleStatus.INITED);
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

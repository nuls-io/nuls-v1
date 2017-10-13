package io.nuls.rpcserver.impl;

import io.nuls.global.NulsContext;
import io.nuls.global.constant.NulsConstant;
import io.nuls.rpcserver.constant.RpcConstant;
import io.nuls.rpcserver.constant.RpcServerConstant;
import io.nuls.rpcserver.impl.services.RpcServerServiceImpl;
import io.nuls.rpcserver.intf.IRpcServerService;
import io.nuls.rpcserver.intf.RpcServerModule;
import io.nuls.task.ModuleStatus;
import io.nuls.task.NulsThread;
import io.nuls.util.log.Log;
import io.nuls.util.str.StringUtils;

import java.util.List;
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

    public RpcServerModuleImpl(){
        super();
        this.ip = getCfgProperty(RpcServerConstant.CFG_RPC_SERVER_SECTION,RpcServerConstant.CFG_RPC_SERVER_IP);
        this.port = getCfgProperty(RpcServerConstant.CFG_RPC_SERVER_SECTION,RpcServerConstant.CFG_RPC_SERVER_PORT);
        this.moduleUrl = getCfgProperty(RpcServerConstant.CFG_RPC_SERVER_SECTION,RpcServerConstant.CFG_RPC_SERVER_URL);
        this.registerService(rpcServerService);
    }

    @Override
    public void start() {
        if(StringUtils.isBlank(ip)|| StringUtils.isBlank(port)){
            rpcServerService.startServer(RpcConstant.DEFAULT_IP,RpcConstant.DEFAULT_PORT,RpcConstant.DEFAULT_URL);
        }else{
            rpcServerService.startServer(ip,Integer.parseInt(port),moduleUrl);
        }
    }

    @Override
    public void shutdown() {
        if(getStatus()!=ModuleStatus.RUNNING){
            return;
        }
        rpcServerService.shutdown();
    }

    public void destroy(){
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

}

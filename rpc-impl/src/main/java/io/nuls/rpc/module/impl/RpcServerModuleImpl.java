package io.nuls.rpc.module.impl;

import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.constant.RpcConstant;
import io.nuls.rpc.module.RpcServerModule;
import io.nuls.rpc.service.impl.RpcServerServiceImpl;
import io.nuls.rpc.service.intf.RpcServerService;

/**
 * Created by Niels on 2017/9/27.
 *
 */
public class RpcServerModuleImpl extends RpcServerModule {

    private RpcServerService rpcServerService = RpcServerServiceImpl.getInstance();
    private String ip;
    private String port;
    private String moduleUrl;

    public RpcServerModuleImpl(){
        super();
        this.ip = getCfgProperty(RpcConstant.CFG_RPC_SECTION,RpcConstant.CFG_RPC_SERVER_IP);
        this.port = getCfgProperty(RpcConstant.CFG_RPC_SECTION,RpcConstant.CFG_RPC_SERVER_PORT);
        this.moduleUrl = getCfgProperty(RpcConstant.CFG_RPC_SECTION,RpcConstant.CFG_RPC_SERVER_URL);
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
        rpcServerService.shutdown();
    }

    @Override
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

    @Override
    public int getVersion() {
        return RpcConstant.RPC_MODULE_VERSION;
    }

}

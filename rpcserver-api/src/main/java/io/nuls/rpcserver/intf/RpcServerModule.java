package io.nuls.rpcserver.intf;


import io.nuls.task.NulsModule;
import io.nuls.task.ModuleStatus;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public abstract class RpcServerModule extends NulsModule{
    protected RpcServerModule(){
        super(RpcServerModule.class.getSimpleName());
    }
}

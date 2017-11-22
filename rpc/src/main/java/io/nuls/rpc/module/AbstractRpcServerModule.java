package io.nuls.rpc.module;


import io.nuls.core.module.BaseNulsModule;

/**
 * Created by Niels on 2017/9/26.
 *
 */
public abstract class AbstractRpcServerModule extends BaseNulsModule {
    protected AbstractRpcServerModule(){
        super("rpc");
    }
}

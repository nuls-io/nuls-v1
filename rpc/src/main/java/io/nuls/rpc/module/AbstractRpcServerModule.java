package io.nuls.rpc.module;


import io.nuls.core.module.BaseNulsModule;

/**
 * @author Niels
 * @date 2017/9/26
 */
public abstract class AbstractRpcServerModule extends BaseNulsModule {
    protected AbstractRpcServerModule() {
        super((short) 9, "rpc");
    }
}

package io.nuls.rpc.module;


import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseNulsModule;

/**
 * @author Niels
 * @date 2017/9/26
 */
public abstract class AbstractRpcServerModule extends BaseNulsModule {
    protected AbstractRpcServerModule() {
        super(NulsConstant.MODULE_ID_RPC);
    }
}

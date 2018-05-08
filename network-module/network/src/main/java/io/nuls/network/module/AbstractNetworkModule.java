package io.nuls.network.module;

import io.nuls.kernel.module.BaseModuleBootstrap;
import io.nuls.network.constant.NetworkConstant;

public abstract class AbstractNetworkModule extends BaseModuleBootstrap {

    protected AbstractNetworkModule() {
        super(NetworkConstant.NETWORK_MODULE_ID);
    }
}

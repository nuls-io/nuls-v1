package io.nuls.network.module;


import io.nuls.core.module.NulsModule;

public abstract class NetworkModule extends NulsModule {
    protected NetworkModule(){
        super(NetworkModule.class.getSimpleName());
    }
}

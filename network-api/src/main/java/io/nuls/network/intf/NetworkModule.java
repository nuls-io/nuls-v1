package io.nuls.network.intf;


import io.nuls.task.NulsModule;
import io.nuls.task.ModuleStatus;

public abstract class NetworkModule extends NulsModule{
    protected NetworkModule(){
        super(NetworkModule.class.getSimpleName());
    }
}

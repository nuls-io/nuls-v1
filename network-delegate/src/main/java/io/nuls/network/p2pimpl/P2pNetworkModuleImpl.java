package io.nuls.network.p2pimpl;

import io.nuls.global.NulsContext;
import io.nuls.network.intf.IPeersManager;
import io.nuls.network.intf.NetworkModule;
import io.nuls.network.p2pimpl.service.P2pPeersManagerImpl;
import io.nuls.task.ModuleStatus;
import io.nuls.util.log.Log;

import java.util.Map;

public class P2pNetworkModuleImpl extends NetworkModule {

    private IPeersManager p2pNetworkManager = P2pPeersManagerImpl.getInstance();

    @Override
    public void init(Map<String, String> initParams) {
        setStatus(ModuleStatus.INITED);
    }

    @Override
    public void start() {
        if(getStatus()== ModuleStatus.UNINITED){
            Log.warn("p2p networke module not init!");
        }
        setStatus(ModuleStatus.STARTING);
        //TODO start the module
        p2pNetworkManager.start();
        setStatus(ModuleStatus.RUNNING);
    }

    @Override
    public void shutdown() {
        if(getStatus()!=ModuleStatus.RUNNING){
            return;
        }
        p2pNetworkManager.shutdown();
        setStatus(ModuleStatus.INITED);
    }

    @Override
    public void desdroy(){
        shutdown();
        NulsContext.getInstance().remService(p2pNetworkManager);
        setStatus(ModuleStatus.UNINITED);
        NulsContext.getInstance().getModuleManager().remModule(this.getModuleName());

    }
    @Override
    public String getInfo() {
        StringBuilder str = new StringBuilder();
        str.append("\nmoduleName:");
        str.append(getModuleName());
        str.append(",p2p module info:");
        str.append("here is info");
        return str.toString();
    }

}

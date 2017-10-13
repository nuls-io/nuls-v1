package io.nuls.network.p2pimpl;

import io.nuls.global.NulsContext;
import io.nuls.network.intf.IPeersManager;
import io.nuls.network.intf.NetworkModule;
import io.nuls.network.p2pimpl.service.P2pPeersManagerImpl;
import io.nuls.task.ModuleStatus;
import io.nuls.task.NulsThread;
import io.nuls.util.log.Log;

import java.util.List;
import java.util.Map;

public class P2pNetworkModuleImpl extends NetworkModule {

    public P2pNetworkModuleImpl(){
        this.registerService(p2pNetworkManager);
    }

    private IPeersManager p2pNetworkManager = P2pPeersManagerImpl.getInstance();


    @Override
    public void start() {
        //TODO start the module
        p2pNetworkManager.start();
    }

    @Override
    public void shutdown() {
        if(getStatus()!=ModuleStatus.RUNNING){
            return;
        }
        p2pNetworkManager.shutdown();
    }

    @Override
    public void destroy(){
        shutdown();

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

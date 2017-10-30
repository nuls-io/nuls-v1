package io.nuls.network.module.impl;


import io.nuls.network.module.NetworkModule;
import io.nuls.network.service.PeersManager;
import io.nuls.network.service.impl.P2pPeersManagerImpl;

public class P2pNetworkModuleImpl extends NetworkModule {

    public P2pNetworkModuleImpl(){
        this.registerService(p2pNetworkManager);
    }

    private PeersManager p2pNetworkManager = P2pPeersManagerImpl.getInstance();

    @Override
    public void start() {
        //TODO start the module
        p2pNetworkManager.start();
    }

    @Override
    public void shutdown() {
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

    @Override
    public String getVersion() {
        return "";
    }
}

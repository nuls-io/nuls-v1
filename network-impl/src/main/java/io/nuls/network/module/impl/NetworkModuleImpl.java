package io.nuls.network.module.impl;


import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.module.NetworkModule;
import io.nuls.network.service.NetworkService;
import io.nuls.network.service.impl.ConnectionManager;
import io.nuls.network.service.impl.NetworkServiceImpl;
import io.nuls.network.service.impl.PeersManager;

public class NetworkModuleImpl extends NetworkModule {

    private NetworkService networkService;

    private NetworkParam network;

    private PeersManager peerManager;

    private ConnectionManager connectionManager;


    public NetworkModuleImpl(){

        networkService = new NetworkServiceImpl(this);
        this.registerService(networkService);
    }



    @Override
    public void start() {
        //TODO start the module
//        peerManager.start();
    }

    @Override
    public void shutdown() {
        networkService.shutdown();
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
    public int getVersion() {
        return 0;
    }




}

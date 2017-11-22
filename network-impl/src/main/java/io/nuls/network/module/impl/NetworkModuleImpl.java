package io.nuls.network.module.impl;


import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.service.NetworkService;
import io.nuls.network.service.impl.NetworkServiceImpl;

import java.io.IOException;

public class NetworkModuleImpl extends AbstractNetworkModule {

    private NetworkService networkService;

    public NetworkModuleImpl() throws IOException {
        ConfigLoader.loadProperties(NetworkConstant.Network_Properties);

        networkService = new NetworkServiceImpl(this);
        this.registerService(networkService);
    }


    @Override
    public void start() {
        //TODO start the module
        networkService.start();
    }

    @Override
    public void shutdown() {
        networkService.shutdown();
    }

    @Override
    public void destroy() {
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

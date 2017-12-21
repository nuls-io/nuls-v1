package io.nuls.network.module.impl;


import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.ReplyNotice;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.service.NetworkService;
import io.nuls.network.service.impl.NetworkServiceImpl;

import java.io.IOException;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkModuleImpl extends AbstractNetworkModule {

    private NetworkService networkService;

    public NetworkModuleImpl() throws IOException {
        super();
        ConfigLoader.loadProperties(NetworkConstant.NETWORK_PROPERTIES);
        networkService = new NetworkServiceImpl(this);
        this.registerService(networkService);
        this.registerBusDataClass((short) 1, ReplyNotice.class);
    }


    @Override
    public void start() {
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

package io.nuls.network.service.impl;

import io.nuls.core.thread.NulsThread;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.module.NetworkModule;
import io.nuls.network.param.DevNetworkParam;
import io.nuls.network.param.MainNetworkParam;
import io.nuls.network.param.TestNetworkParam;
import io.nuls.network.service.NetworkService;

/**
 * Created by win10 on 2017/11/10.
 */
public class NetworkServiceImpl extends NetworkService {

    private NetworkParam network;

    private ConnectionManager connectionManager;

    private PeersManager peersManager;

    public NetworkServiceImpl(NetworkModule module) {
        this.networkModule = module;
        this.network = getNetworkInstance();
        this.connectionManager = new ConnectionManager(module, network);
        this.peersManager = new PeersManager(module, network);
    }


    @Override
    public void start() {
        connectionManager.start();
        peersManager.start();
    }

    @Override
    public void shutdown() {

    }

    private NetworkParam getNetworkInstance() {
        String networkType = ConfigLoader.getCfgValue(NetworkConstant.Network_Section, NetworkConstant.Network_Type, "dev");
        if (networkType.equals("dev")) {
            return DevNetworkParam.get();
        }
        if (networkType.equals("test")) {
            return TestNetworkParam.get();
        }
        return MainNetworkParam.get();
    }


}

package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.NulsThread;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.filter.impl.DefaultMessageFilter;
import io.nuls.network.message.messageFilter.NulsMessageFilter;
import io.nuls.network.module.NetworkModule;
import io.nuls.network.param.DevNetworkParam;
import io.nuls.network.param.MainNetworkParam;
import io.nuls.network.param.TestNetworkParam;
import io.nuls.network.service.NetworkService;

import java.util.Properties;

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
        NulsMessageFilter messageFilter = DefaultMessageFilter.getInstance();
        network.setMessageFilter(messageFilter);

        this.connectionManager = new ConnectionManager(module, network);
        this.peersManager = new PeersManager(module, network, getPeerDao());
        peersManager.setConnectionManager(connectionManager);
        connectionManager.setPeersManager(peersManager);
    }

    @Override
    public void start() {
        try {
            connectionManager.start();
            peersManager.start();
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.NET_SERVER_START_ERROR);
        }
    }

    @Override
    public void shutdown() {

    }

    private NetworkParam getNetworkInstance() {
        String networkType = ConfigLoader.getPropValue(NetworkConstant.Network_Type, "dev");
        if (networkType.equals("dev")) {
            return DevNetworkParam.get();
        }
        if (networkType.equals("test")) {
            return TestNetworkParam.get();
        }
        return MainNetworkParam.get();
    }

    private PeerDao getPeerDao() {
        return null;
    }
//        while (NulsContext.getInstance().getService(PeerDao.class) == null) {
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                Log.error(e);
//            }
//        }
//        return NulsContext.getInstance().getService(PeerDao.class);
//    }
}


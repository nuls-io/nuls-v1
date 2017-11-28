package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.filter.impl.DefaultMessageFilter;
import io.nuls.network.message.messageFilter.NulsMessageFilter;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.param.DevNetworkParam;
import io.nuls.network.param.MainNetworkParam;
import io.nuls.network.param.TestNetworkParam;
import io.nuls.network.service.NetworkService;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkServiceImpl implements NetworkService {

    private AbstractNetworkModule networkModule;
    private AbstractNetworkParam network;

    private ConnectionManager connectionManager;

    private PeersManager peersManager;

    public NetworkServiceImpl(AbstractNetworkModule module) {
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

    @Override
    public long currentTimeMillis() {
        return 0;
    }

    @Override
    public long currentTimeSeconds() {
        return 0;
    }

    private AbstractNetworkParam getNetworkInstance() {
        String networkType = ConfigLoader.getPropValue(NetworkConstant.NETWORK_TYPE, "dev");
        if ("dev".equals(networkType)) {
            return DevNetworkParam.get();
        }
        if ("test".equals(networkType)) {
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


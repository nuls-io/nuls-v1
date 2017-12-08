package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.filter.impl.DefaultMessageFilter;
import io.nuls.network.message.impl.GetPeerDataHandler;
import io.nuls.network.message.impl.PeerDataHandler;
import io.nuls.network.message.messageFilter.NulsMessageFilter;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.param.DevNetworkParam;
import io.nuls.network.param.MainNetworkParam;
import io.nuls.network.param.TestNetworkParam;
import io.nuls.network.service.Broadcaster;
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

    private Broadcaster broadcaster;


    public NetworkServiceImpl(AbstractNetworkModule module) {
        this.networkModule = module;
        this.network = getNetworkInstance();
        NulsMessageFilter messageFilter = DefaultMessageFilter.getInstance();
        network.setMessageFilter(messageFilter);

        this.connectionManager = new ConnectionManager(module, network);
        this.peersManager = new PeersManager(module, network, getPeerDao());
        this.broadcaster = new BroadcasterImpl(peersManager, network);

        peersManager.setConnectionManager(connectionManager);
        connectionManager.setPeersManager(peersManager);

        GetPeerDataHandler.getInstance().setPeersManager(peersManager);
        PeerDataHandler.getInstance().setPeersManager(peersManager);
    }

    @Override
    public void start() {
        try {
            connectionManager.start();
            peersManager.start();

            networkModule.setStatus(ModuleStatusEnum.RUNNING);
        } catch (Exception e) {
            Log.error(e);
            networkModule.setStatus(ModuleStatusEnum.EXCEPTION);
            throw new NulsRuntimeException(ErrorCode.NET_SERVER_START_ERROR);
        }
    }

    @Override
    public void shutdown() {
        connectionManager.serverClose();
//        peersManager.
    }

    @Override
    public BroadcastResult broadcast(BaseNulsEvent event) {
        return broadcaster.broadcast(event);
    }

    @Override
    public BroadcastResult broadcast(byte[] data) {
        return broadcaster.broadcast(data);
    }

    @Override
    public BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName) {
        return broadcaster.broadcastToGroup(event, groupName);
    }

    @Override
    public BroadcastResult broadcastToGroup(byte[] data, String groupName) {
        return broadcaster.broadcastToGroup(data, groupName);
    }

    @Override
    public void addPeer(Peer peer) {
        peersManager.addPeer(peer);
    }

    @Override
    public void addPeerToGroup(String groupName, Peer peer) {
        peersManager.addPeerToGroup(groupName, peer);

    }

    @Override
    public void addPeerGroup(PeerGroup peerGroup){
        peersManager.addPeerGroup(peerGroup);
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
        while (NulsContext.getInstance().getService(PeerDao.class) == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
        return NulsContext.getInstance().getService(PeerDao.class);
    }
}


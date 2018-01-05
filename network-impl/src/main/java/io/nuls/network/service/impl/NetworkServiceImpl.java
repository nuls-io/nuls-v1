package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.NetworkContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.filter.impl.DefaultMessageFilter;
import io.nuls.network.message.impl.GetPeerEventHandler;
import io.nuls.network.message.filter.NulsMessageFilter;
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
        this.peersManager = new PeersManager(module, network, NulsContext.getInstance().getService(PeerDao.class));
        this.broadcaster = new BroadcasterImpl(peersManager, network);

        peersManager.setConnectionManager(connectionManager);
        connectionManager.setPeersManager(peersManager);

        GetPeerEventHandler.getInstance().setPeersManager(peersManager);
//        PeerEventHandler.getInstance().setPeersManager(peersManager);
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
    public boolean isSeedPeer(String peerId) {
        return peersManager.isSeedPeers(peerId);
    }

    @Override
    public boolean isSeedPeer() {
        return peersManager.isSeedPeers(null);
    }

    @Override
    public void addPeer(Peer peer) {
        peersManager.addPeer(peer);
    }

    @Override
    public void removePeer(String peerId) {
        Peer peer = peersManager.getPeer(peerId);
        if (peer == null) {
            throw new NulsRuntimeException(ErrorCode.PEER_NOT_FOUND);
        }
        peersManager.deletePeer(peer);
    }

    @Override
    public void addPeerToGroup(String groupName, Peer peer) {
        peersManager.addPeerToGroup(groupName, peer);
    }

    @Override
    public void addPeerGroup(PeerGroup peerGroup) {
        peersManager.addPeerGroup(peerGroup);
    }


    @Override
    public BroadcastResult broadcast(BaseNetworkEvent event) {
        return broadcaster.broadcast(event);
    }

    @Override
    public BroadcastResult broadcast(BaseNetworkEvent event, String excludePeerId) {
        return broadcaster.broadcast(event, excludePeerId);
    }

    @Override
    public BroadcastResult broadcast(byte[] data) {
        return broadcaster.broadcast(data);
    }

    @Override
    public BroadcastResult broadcast(byte[] data, String excludePeerId) {
        return broadcaster.broadcast(data, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastSync(BaseNetworkEvent event) {
        return broadcaster.broadcast(event);
    }

    @Override
    public BroadcastResult broadcastSync(BaseNetworkEvent event, String excludePeerId) {
        return broadcaster.broadcast(event, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastSync(byte[] data) {
        return broadcaster.broadcast(data);
    }

    @Override
    public BroadcastResult broadcastSync(byte[] data, String excludePeerId) {
        return broadcaster.broadcast(data, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastToPeer(BaseNetworkEvent event, String peerId) {
        return broadcaster.broadcastToPeer(event, peerId);
    }

    @Override
    public BroadcastResult broadcastToPeer(byte[] data, String peerId) {
        return broadcaster.broadcastToPeer(data, peerId);
    }

    @Override
    public BroadcastResult broadcastToGroup(BaseNetworkEvent event, String groupName) {
        return broadcaster.broadcastToGroup(event, groupName);
    }

    @Override
    public BroadcastResult broadcastToGroup(BaseNetworkEvent event, String groupName, String excludePeerId) {
        return broadcaster.broadcastToGroup(event, groupName, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastToGroup(byte[] data, String groupName) {
        return broadcaster.broadcastToGroup(data, groupName);
    }

    @Override
    public BroadcastResult broadcastToGroup(byte[] data, String groupName, String excludePeerId) {
        return broadcaster.broadcastToGroup(data, groupName, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastToGroupSync(BaseNetworkEvent event, String groupName) {
        return broadcaster.broadcast(event, groupName);
    }

    @Override
    public BroadcastResult broadcastToGroupSync(BaseNetworkEvent event, String groupName, String excludePeerId) {
        return broadcaster.broadcastToGroup(event, groupName, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastToGroupSync(byte[] data, String groupName) {
        return broadcaster.broadcastToGroup(data, groupName);
    }

    @Override
    public BroadcastResult broadcastToGroupSync(byte[] data, String groupName, String excludePeerId) {
        return broadcaster.broadcastToGroup(data, groupName, excludePeerId);
    }

    private AbstractNetworkParam getNetworkInstance() {
        String networkType = NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_TYPE, "dev");
        if ("dev".equals(networkType)) {
            return DevNetworkParam.get();
        }
        if ("test".equals(networkType)) {
            return TestNetworkParam.get();
        }
        return MainNetworkParam.get();
    }

}


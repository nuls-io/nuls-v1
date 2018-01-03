package io.nuls.event.bus.service.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.event.bus.event.CommonDigestEvent;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class NetworkEventBroadcasterImpl implements NetworkEventBroadcaster {
    private static NetworkEventBroadcasterImpl INSTANCE = new NetworkEventBroadcasterImpl();

    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);
    private EventCacheService eventCacheService = EventCacheService.getInstance();

    private NetworkEventBroadcasterImpl() {
    }

    public static final NetworkEventBroadcasterImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<String> broadcastHashAndCache(BaseNetworkEvent event) {
        BroadcastResult result = this.networkService.broadcast(new CommonDigestEvent(event.getHash()));
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getPeerIdList(result);
    }

    @Override
    public List<String> broadcastHashAndCache(BaseNetworkEvent event, String excludePeerId) {
        BroadcastResult result = this.networkService.broadcast(new CommonDigestEvent(event.getHash()), excludePeerId);
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getPeerIdList(result);
    }

    private List<String> getPeerIdList(BroadcastResult result) {
        List<String> list = new ArrayList<>();
        if (!result.isSuccess() || result.getBroadcastPeers() == null || result.getBroadcastPeers().isEmpty()) {
            return list;
        }
        for (Peer peer : result.getBroadcastPeers()) {
            list.add(peer.getHash());
        }
        return list;
    }

    @Override
    public List<String> broadcastAndCache(BaseNetworkEvent event, String excludePeerId) {
        BroadcastResult result = networkService.broadcast(event, excludePeerId);
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getPeerIdList(result);
    }

    @Override
    public List<String> broadcastAndCache(BaseNetworkEvent event) {
        BroadcastResult result = networkService.broadcast(event);
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getPeerIdList(result);
    }

    @Override
    public boolean sendToPeer(BaseNetworkEvent event, String peerId) {
        BroadcastResult result = networkService.broadcastToPeer(event, peerId);
        return result.isSuccess();
    }
}

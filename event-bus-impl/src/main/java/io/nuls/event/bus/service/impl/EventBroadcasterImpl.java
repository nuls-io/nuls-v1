package io.nuls.event.bus.service.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.event.CommonHashEvent;
import io.nuls.event.bus.bus.service.intf.EventBroadcaster;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class EventBroadcasterImpl implements EventBroadcaster {
    private static EventBroadcasterImpl INSTANCE = new EventBroadcasterImpl();

    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);
    private EventCacheService eventCacheService = EventCacheService.getInstance();

    private EventBroadcasterImpl() {
    }

    public static final EventBroadcasterImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<String> broadcastHashAndCache(BaseNulsEvent event) {
        BroadcastResult result = this.networkService.broadcast(new CommonHashEvent(event.getHash()));
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getPeerIdList(result);
    }

    @Override
    public List<String> broadcastHashAndCache(BaseNulsEvent event, String excludePeerId) {
        BroadcastResult result = this.networkService.broadcast(new CommonHashEvent(event.getHash()), excludePeerId);
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
    public List<String> broadcastAndCache(BaseNulsEvent event, String excludePeerId) {
        BroadcastResult result = networkService.broadcast(event, excludePeerId);
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getPeerIdList(result);
    }

    @Override
    public List<String> broadcastAndCache(BaseNulsEvent event) {
        BroadcastResult result = networkService.broadcast(event);
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getPeerIdList(result);
    }

    @Override
    public boolean sendToPeer(BaseNulsEvent event, String peerId) {
        BroadcastResult result = networkService.broadcastToPeer(event, peerId);
        return result.isSuccess();
    }
}

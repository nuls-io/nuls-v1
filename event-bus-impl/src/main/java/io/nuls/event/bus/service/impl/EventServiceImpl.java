package io.nuls.event.bus.service.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.event.CommonHashEvent;
import io.nuls.event.bus.event.service.intf.EventService;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class EventServiceImpl implements EventService {
    private static EventServiceImpl INSTANCE = new EventServiceImpl();

    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);
    private EventCacheService eventCacheService = EventCacheService.getInstance();

    private EventServiceImpl() {
    }

    public static final EventServiceImpl getInstance() {
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
    public List<String> broadcast(BaseNulsEvent event, String excludePeerId) {
        BroadcastResult result = networkService.broadcast(event, excludePeerId);
        return getPeerIdList(result);
    }

    @Override
    public List<String> broadcast(BaseNulsEvent event) {
        BroadcastResult result = networkService.broadcast(event);
        return getPeerIdList(result);
    }

    @Override
    public boolean sendToPeer(BaseNulsEvent event, String peerId) {
        BroadcastResult result = networkService.broadcastToPeer(event, peerId);
        return result.isSuccess();
    }
}

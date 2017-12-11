package io.nuls.event.bus.service.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.event.CommonHashEvent;
import io.nuls.event.bus.event.service.intf.EventService;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.service.NetworkService;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class EventServiceImpl implements EventService {
    private static EventServiceImpl INSTANCE = new EventServiceImpl();

    private NetworkService networkService;
    private EventCacheService eventCacheService = EventCacheService.getInstance();

    private EventServiceImpl() {
    }

    public static final EventServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean broadcastSyncNeedConfirmation(BaseNulsEvent event) {
        initNetworkService();
        BroadcastResult result = this.networkService.broadcastSync(new CommonHashEvent(event.getHash()));
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return result.isSuccess();
    }

    @Override
    public boolean broadcastHashAndCache(BaseNulsEvent event) {
        initNetworkService();
        BroadcastResult result = this.networkService.broadcast(new CommonHashEvent(event.getHash()));
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return result.isSuccess();
    }

    @Override
    public void broadcast(BaseNulsEvent event, String excludePeerId) {
        initNetworkService();
        networkService.broadcast(event, excludePeerId);
    }

    @Override
    public void broadcast(BaseNulsEvent event) {
        initNetworkService();
        networkService.broadcast(event);
    }

    @Override
    public void sendToPeer(BaseNulsEvent event, String peerId) {
        initNetworkService();
        networkService.broadcastToPeer(event, peerId);
    }

    private void initNetworkService() {
        if (networkService == null) {
            networkService = NulsContext.getInstance().getService(NetworkService.class);
        }
    }
}

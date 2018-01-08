package io.nuls.event.bus.service.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseEvent;
import io.nuls.event.bus.event.CommonDigestEvent;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
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
    private EventBusService eventBusService = EventBusServiceImpl.getInstance();

    private EventBroadcasterImpl() {
    }

    public static final EventBroadcasterImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<String> broadcastHashAndCache(BaseEvent event, boolean needToSelf) {
        BroadcastResult result = this.networkService.sendToAllNode(new CommonDigestEvent(event.getHash()));
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }

    @Override
    public List<String> broadcastHashAndCache(BaseEvent event, boolean needToSelf, String excludeNodeId) {
        BroadcastResult result = this.networkService.sendToAllNode(new CommonDigestEvent(event.getHash()), excludeNodeId);
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }

    private List<String> getNodeIdList(BroadcastResult result) {
        List<String> list = new ArrayList<>();
        if (!result.isSuccess() || result.getBroadcastNodes() == null || result.getBroadcastNodes().isEmpty()) {
            return list;
        }
        for (Node node : result.getBroadcastNodes()) {
            list.add(node.getHash());
        }
        return list;
    }

    @Override
    public List<String> broadcastAndCache(BaseEvent event, boolean needToSelf, String excludeNodeId) {
        BroadcastResult result = networkService.sendToAllNode(event, excludeNodeId);
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }

    @Override
    public List<String> broadcastAndCache(BaseEvent event, boolean needToSelf) {
        BroadcastResult result = networkService.sendToAllNode(event);
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }

    @Override
    public boolean sendToNode(BaseEvent event, String nodeId) {
        BroadcastResult result = networkService.sendToNode(event, nodeId);
        return result.isSuccess();
    }
}

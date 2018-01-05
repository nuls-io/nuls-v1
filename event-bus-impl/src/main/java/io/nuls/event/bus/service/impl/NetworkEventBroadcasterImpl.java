package io.nuls.event.bus.service.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.event.bus.event.CommonDigestEvent;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
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
        BroadcastResult result = this.networkService.sendToAllNode(new CommonDigestEvent(event.getHash()));
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }

    @Override
    public List<String> broadcastHashAndCache(BaseNetworkEvent event, String excludeNodeId) {
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
    public List<String> broadcastAndCache(BaseNetworkEvent event, String excludeNodeId) {
        BroadcastResult result = networkService.sendToAllNode(event, excludeNodeId);
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }

    @Override
    public List<String> broadcastAndCache(BaseNetworkEvent event) {
        BroadcastResult result = networkService.sendToAllNode(event);
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }

    @Override
    public boolean sendToNode(BaseNetworkEvent event, String nodeId) {
        BroadcastResult result = networkService.sendToNode(event, nodeId);
        return result.isSuccess();
    }
}

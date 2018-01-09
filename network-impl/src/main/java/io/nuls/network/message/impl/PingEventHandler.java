package io.nuls.network.message.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkCacheService;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.PongEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;

public class PingEventHandler implements NetWorkEventHandler {

    private static PingEventHandler handler = new PingEventHandler();

    private NetworkCacheService cacheService;

    private PingEventHandler() {
        cacheService = NetworkCacheService.getInstance();
    }

    public static PingEventHandler getInstance() {
        return handler;
    }

    @Override
    public NetworkEventResult process(BaseEvent event, Node node) {

        String key = event.getHeader().getEventType() + "-" + node.getIp() + "-" + node.getPort();
        if (cacheService.existEvent(key)) {
            node.destroy();
            return null;
        }
        cacheService.putEvent(key, event, true);
        NetworkEventResult result = new NetworkEventResult(true, new PongEvent());
        return result;
    }
}

package io.nuls.network.message.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkCacheService;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.handler.NetWorkEventHandler;

public class PongEventHandler implements NetWorkEventHandler {

    private static PongEventHandler handler = new PongEventHandler();

    private NetworkCacheService cacheService;

    private PongEventHandler() {
        cacheService = NetworkCacheService.getInstance();
    }

    public static PongEventHandler getInstance() {
        return handler;
    }

    /**
     * Heartbeat every 6 seconds
     *
     * @param event
     * @param node
     * @return
     */
    @Override
    public NetworkEventResult process(BaseEvent event, Node node) {

        String key = event.getHeader().getEventType() + "-" + node.getIp() + "-" + node.getPort();
        if (cacheService.existEvent(key)) {
            node.destroy();
            return null;
        }
        cacheService.putEvent(key, event, true);
        return null;
    }
}

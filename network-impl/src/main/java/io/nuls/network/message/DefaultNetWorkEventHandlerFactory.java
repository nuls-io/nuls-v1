package io.nuls.network.message;

import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class DefaultNetWorkEventHandlerFactory extends NetworkEventHandlerFactory {

    private static final DefaultNetWorkEventHandlerFactory INSTANCE = new DefaultNetWorkEventHandlerFactory();

    private Map<String, NetWorkEventHandler> handlerMap = new HashMap<>();

    private DefaultNetWorkEventHandlerFactory() {
//        handlerMap.put(VersionEvent.class.getName(), VersionEventHandler.getInstance());
//        handlerMap.put(GetVersionEvent.class.getName(), GetVersionEventHandler.getInstance());
//        handlerMap.put(GetPeerEvent.class.getName(), GetPeerEventHandler.getInstance());
//        handlerMap.put(PeerEvent.class.getName(), PeerEventHandler.getInstance());
    }

    public static NetworkEventHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public NetWorkEventHandler getHandler(BaseNetworkEvent event) {
        return handlerMap.get(event.getClass().getName());
    }
}

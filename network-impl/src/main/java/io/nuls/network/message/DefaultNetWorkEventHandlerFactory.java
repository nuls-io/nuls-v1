package io.nuls.network.message;

import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.network.message.entity.GetPeerEvent;
import io.nuls.network.message.entity.GetVersionEvent;
import io.nuls.network.message.entity.PeerEvent;
import io.nuls.network.message.entity.VersionData;
import io.nuls.network.message.impl.GetPeerEventHandler;
import io.nuls.network.message.impl.GetVersionDataHandler;
import io.nuls.network.message.impl.PeerDataHandler;
import io.nuls.network.message.impl.VersionDataHandler;
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
//        handlerMap.put(VersionData.class.getName(), VersionDataHandler.getInstance());
//        handlerMap.put(GetVersionEvent.class.getName(), GetVersionDataHandler.getInstance());
//        handlerMap.put(GetPeerEvent.class.getName(), GetPeerEventHandler.getInstance());
//        handlerMap.put(PeerEvent.class.getName(), PeerDataHandler.getInstance());
    }

    public static NetworkEventHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public NetWorkEventHandler getHandler(BaseNetworkEvent event) {
        return handlerMap.get(event.getClass().getName());
    }
}

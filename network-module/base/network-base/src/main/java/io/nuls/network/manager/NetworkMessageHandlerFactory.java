package io.nuls.network.manager;

import io.nuls.network.message.impl.*;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.*;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.HashMap;
import java.util.Map;

public class NetworkMessageHandlerFactory {

    private Map<String, BaseNetworkMeesageHandler> handlerMap = new HashMap<>();

    private static NetworkMessageHandlerFactory INSTANCE = new NetworkMessageHandlerFactory();

    public static NetworkMessageHandlerFactory getInstance() {
        return INSTANCE;
    }

    private NetworkMessageHandlerFactory() {
        handlerMap.put(HandshakeMessage.class.getName(), HandshakeMessageHandler.getInstance());
        handlerMap.put(GetVersionMessage.class.getName(), GetVersionMessageHandler.getInstance());
        handlerMap.put(VersionMessage.class.getName(), VersionMessageHandler.getInstance());
        handlerMap.put(GetNodesMessage.class.getName(), GetNodesMessageHandler.getInstance());
        handlerMap.put(NodesMessage.class.getName(), NodesMessageHandler.getInstance());
        handlerMap.put(GetNodesIpMessage.class.getName(), GetNodesIpMessageHandler.getInstance());
        handlerMap.put(NodesIpMessage.class.getName(), NodesIpMessageHandler.getInstance());
    }

    public BaseNetworkMeesageHandler getHandler(BaseMessage message) {
        return handlerMap.get(message.getClass().getName());
    }
}

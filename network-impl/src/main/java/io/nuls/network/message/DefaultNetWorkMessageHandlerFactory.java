package io.nuls.network.message;

import io.nuls.network.message.entity.VersionMessage;
import io.nuls.network.message.impl.VersionMessageHandler;
import io.nuls.network.message.messageHandler.NetWorkMessageHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vivi on 2017/11/21.
 */
public class DefaultNetWorkMessageHandlerFactory extends AbstractNetWorkMessageHandlerFactory {

    private static final DefaultNetWorkMessageHandlerFactory INSTANCE = new DefaultNetWorkMessageHandlerFactory();

    private Map<String, NetWorkMessageHandler> handlerMap = new HashMap<>();

    private DefaultNetWorkMessageHandlerFactory() {
        handlerMap.put(VersionMessage.class.getName(), VersionMessageHandler.getInstance());
        //handlerMap.put(PingMessage.class.getName(),)
    }

    public static DefaultNetWorkMessageHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public NetWorkMessageHandler getHandler(AbstractNetworkMessage message) {
        return handlerMap.get(message.getClass().getName());
    }
}

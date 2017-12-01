package io.nuls.network.message;

import io.nuls.network.message.entity.GetPeerData;
import io.nuls.network.message.entity.GetVersionData;
import io.nuls.network.message.entity.VersionData;
import io.nuls.network.message.impl.GetVersionDataHandler;
import io.nuls.network.message.impl.VersionDataHandler;
import io.nuls.network.message.messageHandler.NetWorkDataHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class DefaultNetWorkDataHandlerFactory extends AbstractNetWorkDataHandlerFactory {

    private static final DefaultNetWorkDataHandlerFactory INSTANCE = new DefaultNetWorkDataHandlerFactory();

    private Map<String, NetWorkDataHandler> handlerMap = new HashMap<>();

    private DefaultNetWorkDataHandlerFactory() {
        handlerMap.put(VersionData.class.getName(), VersionDataHandler.getInstance());
        handlerMap.put(GetVersionData.class.getName(), GetVersionDataHandler.getInstance());
        handlerMap.put(GetPeerData.class.getName(), GetVersionDataHandler.getInstance());
    }

    public static DefaultNetWorkDataHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public NetWorkDataHandler getHandler(BaseNetworkData data) {
        return handlerMap.get(data.getClass().getName());
    }
}

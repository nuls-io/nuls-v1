package io.nuls.network.message;

import io.nuls.network.message.messageHandler.NetWorkMessageHandler;

/**
 *
 * @author vivi
 * @date 2017/11/21
 */
public abstract class AbstractNetWorkMessageHandlerFactory {

    public abstract NetWorkMessageHandler getHandler(AbstractNetworkMessage message);
}

package io.nuls.network.message;

import io.nuls.core.mesasge.NulsMessage;
import io.nuls.network.message.messageHandler.NetWorkMessageHandler;

/**
 * Created by vivi on 2017/11/21.
 */
public abstract class NetWorkMessageHandlerFactory {

    public abstract NetWorkMessageHandler getHandler(NetworkMessage message);
}

package io.nuls.network.message;

import io.nuls.network.message.messageHandler.NetWorkDataHandler;

/**
 * @author vivi
 * @date 2017/11/21
 */
public abstract class AbstractNetWorkDataHandlerFactory {


    public abstract NetWorkDataHandler getHandler(BaseNetworkData data) ;
}

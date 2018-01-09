package io.nuls.network.message;

import io.nuls.core.event.BaseEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;

/**
 * @author vivi
 * @date 2017/11/21
 */
public abstract class NetworkEventHandlerFactory {


    public abstract NetWorkEventHandler getHandler(BaseEvent data) ;
}

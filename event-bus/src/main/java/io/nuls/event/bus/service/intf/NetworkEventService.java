package io.nuls.event.bus.service.intf;

import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;

/**
 * @author Niels
 * @date 2017/11/6
 */
public interface NetworkEventService {

    void publish(byte[] event, String peerHash);

    void publish(BaseNetworkEvent event, String peerHash);

    String registerEventHandler(Class<? extends BaseNetworkEvent> eventClass, AbstractNetworkEventHandler<? extends BaseNetworkEvent> handler);

    void removeEventHandler(String handlerId);

    void shutdown();
}

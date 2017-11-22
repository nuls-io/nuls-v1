package io.nuls.event.bus.processor.service.intf;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 * Created by Niels on 2017/11/6.
 */
public interface NetworkProcessorService {

    void send(byte[] event);

    String registerEventHandler(Class<? extends BaseNulsEvent> eventClass, AbstractNetworkNulsEventHandler<? extends BaseNulsEvent> handler);

    void removeEventHandler(String handlerId);

    void shutdown();
}

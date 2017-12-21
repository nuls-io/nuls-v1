package io.nuls.event.bus.processor.service.intf;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;

/**
 * @author Niels
 * @date 2017/11/6
 */
public interface EventProcessorService {

    void send(byte[] event,String peerHash);

    String registerEventHandler(Class<? extends BaseNulsEvent> eventClass, AbstractEventBusHandler<? extends BaseNulsEvent> handler);

    void removeEventHandler(String handlerId);

    void shutdown();
}

package io.nuls.event.bus.processor.service.intf;

import io.nuls.core.event.NulsEvent;
import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;

/**
 * Created by Niels on 2017/11/6.
 */
public interface NetworkProcessorService {

    void send(byte[] event);

    String registerEventHandler(Class<? extends NulsEvent> eventClass, NetworkNulsEventHandler<? extends NulsEvent> handler);

    void removeEventHandler(String handlerId);

    void shutdown();
}

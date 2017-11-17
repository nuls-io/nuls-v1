package io.nuls.event.bus.processor.service.intf;

import io.nuls.core.event.NulsEvent;
import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;

/**
 * Created by Niels on 2017/11/6.
 */
public interface NetworkProcessorService {

    public void send(byte[] event);

    public String registerEventHandler(Class<? extends NulsEvent> eventClass, NetworkNulsEventHandler<? extends NulsEvent> handler);

    public void removeEventHandler(String handlerId);

    void shutdown();
}

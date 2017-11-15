package io.nuls.event.bus.processor.service.intf;

import io.nuls.core.event.NulsEvent;
import io.nuls.event.bus.event.handler.LocalNulsEventHandler;

/**
 * Created by Niels on 2017/11/6.
 */
public interface LocalProcessorService {

    public void send(NulsEvent event);

    public String registerEventHandler(Class<? extends NulsEvent> eventClass, LocalNulsEventHandler<? extends NulsEvent> handler);

    public void removeEventHandler(String handlerId);

    void shutdown();
}

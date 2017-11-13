package io.nuls.event.bus.processor.service.intf;

import io.nuls.core.event.NulsEvent;
import io.nuls.event.bus.event.handler.NulsEventHandler;

/**
 * Created by Niels on 2017/11/6.
 * nuls.io
 */
public interface ProcessorService {

    public void send(NulsEvent event);

    public String registerEventHandler(Class<? extends NulsEvent> eventClass, NulsEventHandler<? extends NulsEvent> handler);

    public void removeEventHandler(String handlerId);
}

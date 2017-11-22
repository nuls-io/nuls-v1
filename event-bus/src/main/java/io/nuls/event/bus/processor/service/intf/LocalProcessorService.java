package io.nuls.event.bus.processor.service.intf;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.event.handler.AbstractLocalNulsEventHandler;

/**
 * Created by Niels on 2017/11/6.
 */
public interface LocalProcessorService {

    public void send(BaseNulsEvent event);

    public String registerEventHandler(Class<? extends BaseNulsEvent> eventClass, AbstractLocalNulsEventHandler<? extends BaseNulsEvent> handler);

    public void removeEventHandler(String handlerId);

    void shutdown();
}

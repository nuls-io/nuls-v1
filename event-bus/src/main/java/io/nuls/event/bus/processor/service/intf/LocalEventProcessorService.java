package io.nuls.event.bus.processor.service.intf;

import io.nuls.core.event.BaseLocalEvent;
import io.nuls.event.bus.handler.AbstractLocalEventHandler;

/**
 * @author Niels
 * @date 2017/11/6
 */
public interface LocalEventProcessorService {

    public void dispatch(BaseLocalEvent data);

    public String registerEventHandler(Class<? extends BaseLocalEvent> eventClass, AbstractLocalEventHandler<? extends BaseLocalEvent> handler);

    public void removeEventHandler(String handlerId);

    void shutdown();
}

package io.nuls.event.bus.service.intf;

import io.nuls.core.event.BaseLocalEvent;
import io.nuls.event.bus.handler.AbstractLocalEventHandler;

/**
 * @author Niels
 * @date 2017/11/6
 */
public interface LocalEventService {

    public void publish(BaseLocalEvent data);

    public String registerEventHandler(Class<? extends BaseLocalEvent> eventClass, AbstractLocalEventHandler<? extends BaseLocalEvent> handler);

    public void removeEventHandler(String handlerId);

    void shutdown();
}

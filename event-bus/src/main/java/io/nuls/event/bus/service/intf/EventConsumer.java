package io.nuls.event.bus.service.intf;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.BaseLocalEvent;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.event.bus.constant.EventCategoryEnum;
import io.nuls.event.bus.handler.intf.NulsEventHandler;

/**
 * @author Niels
 * @date 2018/1/5
 */
public interface EventConsumer {

    String subscribeEvent(EventCategoryEnum category, Class<? extends BaseEvent> eventClass, NulsEventHandler<? extends BaseEvent> eventHandler);

    String subscribeLocalEvent(Class<? extends BaseLocalEvent> eventClass, NulsEventHandler<? extends BaseLocalEvent> eventHandler);

    String subscribeNetworkEvent(Class<? extends BaseNetworkEvent> eventClass, NulsEventHandler<? extends BaseNetworkEvent> eventHandler);

    void unsubscribeEvent(String subcribeId);
}

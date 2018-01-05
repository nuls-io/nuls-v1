package io.nuls.event.bus.service.intf;

import io.nuls.core.event.BaseEvent;
import io.nuls.event.bus.constant.EventCategoryEnum;
import io.nuls.event.bus.handler.intf.NulsEventHandler;

/**
 * @author Niels
 * @date 2018/1/5
 */
public interface EventConsumer {

    String subscribeEvent(EventCategoryEnum category, BaseEvent event, NulsEventHandler<? extends BaseEvent> eventHandler);

    String subscribeLocalEvent(Class<? extends BaseEvent> eventClass, NulsEventHandler<? extends BaseEvent> eventHandler);

    String subscribeNetworkEvent(Class<? extends BaseEvent> eventClass, NulsEventHandler<? extends BaseEvent> eventHandler);

    void unsubscribeEvent(String subcribeId);
}

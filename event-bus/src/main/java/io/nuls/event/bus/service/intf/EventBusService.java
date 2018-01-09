package io.nuls.event.bus.service.intf;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.constant.EventCategoryEnum;
import io.nuls.event.bus.handler.intf.NulsEventHandler;

/**
 * @author Niels
 * @date 2018/1/5
 */
public interface EventBusService {

    String subscribeEvent( Class<? extends BaseEvent> eventClass, NulsEventHandler<? extends BaseEvent> eventHandler);

    void unsubscribeEvent(String subcribeId);

    void publishEvent(EventCategoryEnum category, byte[] bytes, String fromId) throws IllegalAccessException, NulsException, InstantiationException;

    void publishEvent(EventCategoryEnum category, BaseEvent event, String fromId);

    void publishNetworkEvent(byte[] bytes, String fromId);

    void publishNetworkEvent(BaseEvent event, String fromId);

    void publishLocalEvent(BaseEvent event);
}

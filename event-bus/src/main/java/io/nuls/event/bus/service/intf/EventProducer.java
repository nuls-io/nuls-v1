package io.nuls.event.bus.service.intf;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.BaseLocalEvent;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.constant.EventCategoryEnum;

/**
 * @author Niels
 * @date 2018/1/3
 */
public interface EventProducer {

    void publish(EventCategoryEnum category, byte[] bytes, String fromId) throws IllegalAccessException, NulsException, InstantiationException;

    void publish(EventCategoryEnum category, BaseEvent event, String fromId);

    void publishNetworkEvent(byte[] bytes, String fromId);

    void publishNetworkEvent(BaseNetworkEvent event, String fromId);

    void publishLocalEvent(BaseLocalEvent event);
}

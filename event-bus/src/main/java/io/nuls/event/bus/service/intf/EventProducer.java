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

    void send(EventCategoryEnum category, byte[] bytes, String fromId) throws IllegalAccessException, NulsException, InstantiationException;

    void send(EventCategoryEnum category, BaseEvent event, String fromId);

    void sendNetworkEvent(byte[] bytes, String fromId);

    void sendNetworkEvent(BaseNetworkEvent event, String fromId);

    void sendLocalEvent(BaseLocalEvent event);
}

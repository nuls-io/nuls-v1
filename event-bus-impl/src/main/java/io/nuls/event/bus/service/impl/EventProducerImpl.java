package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.BaseLocalEvent;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.constant.EventCategoryEnum;
import io.nuls.event.bus.service.intf.LocalEventService;
import io.nuls.event.bus.service.intf.NetworkEventService;
import io.nuls.event.bus.service.intf.EventProducer;

/**
 * @author Niels
 * @date 2018/1/3
 */
public class EventProducerImpl implements EventProducer {
    private static EventProducer INSTANCE = new EventProducerImpl();

    private LocalEventService localService = LocalEventServiceImpl.getInstance();
    private NetworkEventService networkService = NetworkEventServiceImpl.getInstance();

    private EventProducerImpl() {
    }

    public static EventProducer getInstance() {
        return INSTANCE;
    }

    @Override
    public void publish(EventCategoryEnum category, byte[] bytes, String fromId) throws IllegalAccessException, NulsException, InstantiationException {
        if (category == EventCategoryEnum.LOCAL) {
            BaseLocalEvent event = EventManager.getLocalEventInstance(bytes);
            this.publishLocalEvent(event);
        } else {
            this.publishNetworkEvent(bytes, fromId);
        }
    }

    @Override
    public void publish(EventCategoryEnum category, BaseEvent event, String fromId) {
        if (category == EventCategoryEnum.LOCAL) {
            this.publishLocalEvent((BaseLocalEvent) event);
        } else {
            this.publishNetworkEvent((BaseNetworkEvent) event, fromId);
        }
    }

    @Override
    public void publishNetworkEvent(byte[] bytes, String fromId) {
        networkService.publish(bytes, fromId);
    }

    @Override
    public void publishNetworkEvent(BaseNetworkEvent event, String fromId) {
        networkService.publish(event, fromId);
    }

    @Override
    public void publishLocalEvent(BaseLocalEvent event) {
        localService.publish(event);
    }
}

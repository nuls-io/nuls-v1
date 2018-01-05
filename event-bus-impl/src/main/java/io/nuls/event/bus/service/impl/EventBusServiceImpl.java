package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.BaseLocalEvent;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.constant.EventCategoryEnum;
import io.nuls.event.bus.handler.AbstractLocalEventHandler;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.handler.intf.NulsEventHandler;
import io.nuls.event.bus.service.intf.EventBusService;

/**
 * @author Niels
 * @date 2018/1/5
 */
public class EventBusServiceImpl implements EventBusService {

    private static EventBusService INSTANCE = new EventBusServiceImpl();
    private LocalEventService localService = LocalEventService.getInstance();
    private NetworkEventService networkService = NetworkEventService.getInstance();

    private EventBusServiceImpl() {
    }

    public static EventBusService getInstance() {
        return INSTANCE;
    }

    @Override
    public String subscribeEvent(EventCategoryEnum category, Class<? extends BaseEvent> eventClass, NulsEventHandler<? extends BaseEvent> eventHandler) {
        String id = null;
        if (category == EventCategoryEnum.LOCAL) {
            id = localService.registerEventHandler((Class<? extends BaseLocalEvent>) eventClass,
                    (AbstractLocalEventHandler<? extends BaseLocalEvent>) eventHandler);
        } else if (category == EventCategoryEnum.NETWORK) {
            id = networkService.registerEventHandler((Class<? extends BaseNetworkEvent>) eventClass,
                    (AbstractNetworkEventHandler<? extends BaseNetworkEvent>) eventHandler);
        }
        return id;
    }

    @Override
    public String subscribeLocalEvent(Class<? extends BaseLocalEvent> eventClass, NulsEventHandler<? extends BaseLocalEvent> eventHandler) {
        return this.subscribeEvent(EventCategoryEnum.LOCAL, eventClass, eventHandler);
    }

    @Override
    public String subscribeNetworkEvent(Class<? extends BaseNetworkEvent> eventClass, NulsEventHandler<? extends BaseNetworkEvent> eventHandler) {
        return this.subscribeEvent(EventCategoryEnum.NETWORK, eventClass, eventHandler);
    }

    @Override
    public void unsubscribeEvent(String subcribeId) {
        this.localService.removeEventHandler(subcribeId);
        this.networkService.removeEventHandler(subcribeId);
    }

    @Override
    public void publishEvent(EventCategoryEnum category, byte[] bytes, String fromId) throws IllegalAccessException, NulsException, InstantiationException {
        if (category == EventCategoryEnum.LOCAL) {
            BaseLocalEvent event = EventManager.getLocalEventInstance(bytes);
            this.publishLocalEvent(event);
        } else {
            this.publishNetworkEvent(bytes, fromId);
        }
    }

    @Override
    public void publishEvent(EventCategoryEnum category, BaseEvent event, String fromId) {
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

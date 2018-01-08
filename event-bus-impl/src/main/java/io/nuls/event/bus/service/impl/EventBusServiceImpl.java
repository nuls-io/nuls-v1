package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.constant.EventCategoryEnum;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.handler.AbstractEventHandler;
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
    public String subscribeEvent(Class<? extends BaseEvent> eventClass, NulsEventHandler<? extends BaseEvent> eventHandler) {
        String id = localService.registerEventHandler(eventClass,
                (AbstractEventHandler<? extends BaseEvent>) eventHandler);
        networkService.registerEventHandler(id,eventClass,
                (AbstractEventHandler<? extends BaseEvent>) eventHandler);
        return id;
    }


    @Override
    public void unsubscribeEvent(String subcribeId) {
        this.localService.removeEventHandler(subcribeId);
        this.networkService.removeEventHandler(subcribeId);
    }

    @Override
    public void publishEvent(EventCategoryEnum category, byte[] bytes, String fromId) throws IllegalAccessException, NulsException, InstantiationException {
        if (category == EventCategoryEnum.LOCAL) {
            BaseEvent event = EventManager.getInstance(bytes);
            this.publishLocalEvent(event);
        } else {
            this.publishNetworkEvent(bytes, fromId);
        }
    }

    @Override
    public void publishEvent(EventCategoryEnum category, BaseEvent event, String fromId) {
        if (category == EventCategoryEnum.LOCAL) {
            this.publishLocalEvent((BaseEvent) event);
        } else {
            this.publishNetworkEvent((BaseEvent) event, fromId);
        }
    }

    @Override
    public void publishNetworkEvent(byte[] bytes, String fromId) {
        networkService.publish(bytes, fromId);
    }

    @Override
    public void publishNetworkEvent(BaseEvent event, String fromId) {
        networkService.publish(event, fromId);
    }

    @Override
    public void publishLocalEvent(BaseEvent event) {
        localService.publish(event);
    }

}

package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.BaseLocalEvent;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.event.bus.constant.EventCategoryEnum;
import io.nuls.event.bus.handler.AbstractLocalEventHandler;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.handler.intf.NulsEventHandler;
import io.nuls.event.bus.service.intf.EventConsumer;

/**
 * @author Niels
 * @date 2018/1/5
 */
public class EventConsumerImpl implements EventConsumer {

    private static EventConsumer INSTANCE = new EventConsumerImpl();
    private LocalEventService localService = LocalEventService.getInstance();
    private NetworkEventService networkService = NetworkEventService.getInstance();

    private EventConsumerImpl() {
    }

    public static EventConsumer getInstance() {
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

}

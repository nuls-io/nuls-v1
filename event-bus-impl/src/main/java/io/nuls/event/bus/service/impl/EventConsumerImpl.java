package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.event.bus.constant.EventCategoryEnum;
import io.nuls.event.bus.handler.intf.NulsEventHandler;
import io.nuls.event.bus.service.intf.EventConsumer;

/**
 * @author Niels
 * @date 2018/1/5
 */
public class EventConsumerImpl implements EventConsumer {

    private static EventConsumer INSTANCE = new EventConsumerImpl();

    private EventConsumerImpl() {
    }

    public static EventConsumer getInstance() {
        return INSTANCE;
    }

    @Override
    public String subscribeEvent(EventCategoryEnum category, BaseEvent event, NulsEventHandler<? extends BaseEvent> eventHandler) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public String subscribeLocalEvent(Class<? extends BaseEvent> eventClass, NulsEventHandler<? extends BaseEvent> eventHandler) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public String subscribeNetworkEvent(Class<? extends BaseEvent> eventClass, NulsEventHandler<? extends BaseEvent> eventHandler) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public void unsubscribeEvent(String subcribeId) {
        // todo auto-generated method stub(niels)

    }

}

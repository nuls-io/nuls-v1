package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.BaseLocalEvent;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.constant.EventCategoryEnum;
import io.nuls.event.bus.processor.service.impl.LocalEventProcessorServiceImpl;
import io.nuls.event.bus.processor.service.impl.NetworkEventProcessorServiceImpl;
import io.nuls.event.bus.processor.service.intf.LocalEventProcessorService;
import io.nuls.event.bus.processor.service.intf.NetworkEventProcessorService;
import io.nuls.event.bus.service.intf.EventProducer;

/**
 * @author Niels
 * @date 2018/1/3
 */
public class EventProducerImpl implements EventProducer {
    private static EventProducer INSTANCE = new EventProducerImpl();

    private LocalEventProcessorService localService = LocalEventProcessorServiceImpl.getInstance();
    private NetworkEventProcessorService networkService = NetworkEventProcessorServiceImpl.getInstance();

    private EventProducerImpl() {
    }

    public static EventProducer getInstance() {
        return INSTANCE;
    }

    @Override
    public void dispatch(EventCategoryEnum category, byte[] bytes, String fromId) throws IllegalAccessException, NulsException, InstantiationException {
        if (category == EventCategoryEnum.LOCAL) {
            BaseLocalEvent event = EventManager.getLocalEventInstance(bytes);
            this.dispatchLocalEvent(event);
        } else {
            this.dispatchNetworkEvent(bytes, fromId);
        }
    }

    @Override
    public void dispatch(EventCategoryEnum category, BaseEvent event, String fromId) {
        if (category == EventCategoryEnum.LOCAL) {
            this.dispatchLocalEvent((BaseLocalEvent) event);
        } else {
            this.dispatchNetworkEvent((BaseNetworkEvent) event, fromId);
        }
    }

    @Override
    public void dispatchNetworkEvent(byte[] bytes, String fromId) {
        networkService.dispatch(bytes, fromId);
    }

    @Override
    public void dispatchNetworkEvent(BaseNetworkEvent event, String fromId) {
        networkService.dispatch(event, fromId);
    }

    @Override
    public void dispatchLocalEvent(BaseLocalEvent event) {
        localService.dispatch(event);
    }
}

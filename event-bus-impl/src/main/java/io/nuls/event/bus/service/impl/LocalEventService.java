package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.processor.manager.ProcessorManager;

/**
 * @author Niels
 * @date 2017/11/3
 */
public class LocalEventService {

    private static final LocalEventService INSTANCE = new LocalEventService();
    private final ProcessorManager processorManager;

    private LocalEventService() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_LOCAL);
    }

    public static LocalEventService getInstance() {
        return INSTANCE;
    }

    public void publish(BaseEvent event) {
        processorManager.offer(new ProcessData(event));
    }

    public String registerEventHandler(Class<? extends BaseEvent> eventClass, AbstractEventHandler<? extends BaseEvent> handler) {
        return this.registerEventHandler(null, eventClass, handler);
    }

    public String registerEventHandler(String handlerId, Class<? extends BaseEvent> eventClass, AbstractEventHandler<? extends BaseEvent> handler) {
        return processorManager.registerEventHandler(handlerId, eventClass, handler);
    }

    public void removeEventHandler(String handlerId) {
        processorManager.removeEventHandler(handlerId);
    }

    public void shutdown() {
        this.processorManager.shutdown();
    }
}

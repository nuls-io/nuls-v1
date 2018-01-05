package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseLocalEvent;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.handler.AbstractLocalEventHandler;
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

    public void publish(BaseLocalEvent event) {
        processorManager.offer(new ProcessData(event));
    }

    public String registerEventHandler(Class<? extends BaseLocalEvent> eventClass, AbstractLocalEventHandler<? extends BaseLocalEvent> handler) {
        return processorManager.registerEventHandler(eventClass, handler);
    }

    public void removeEventHandler(String handlerId) {
        processorManager.removeEventHandler(handlerId);
    }

    public void shutdown() {
        this.processorManager.shutdown();
    }
}

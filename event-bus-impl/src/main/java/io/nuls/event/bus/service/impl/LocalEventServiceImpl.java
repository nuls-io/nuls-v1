package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseLocalEvent;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.handler.AbstractLocalEventHandler;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.service.intf.LocalEventService;

/**
 *
 * @author Niels
 * @date 2017/11/3
 */
public class LocalEventServiceImpl implements LocalEventService {

    private static final LocalEventServiceImpl INSTANCE = new LocalEventServiceImpl();
    private final ProcessorManager processorManager;

    private LocalEventServiceImpl() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_LOCAL);
    }

    public static LocalEventService getInstance() {
        return INSTANCE;
    }

    @Override
    public void publish(BaseLocalEvent event) {
        processorManager.offer(new ProcessData(event));
    }

    @Override
    public String registerEventHandler(Class<? extends BaseLocalEvent> eventClass, AbstractLocalEventHandler<? extends BaseLocalEvent> handler) {
        return processorManager.registerEventHandler(eventClass, handler);
    }

    @Override
    public void removeEventHandler(String handlerId) {
        processorManager.removeEventHandler(handlerId);
    }

    @Override
    public void shutdown() {
        this.processorManager.shutdown();
    }
}

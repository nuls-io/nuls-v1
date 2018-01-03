package io.nuls.event.bus.processor.service.impl;

import io.nuls.core.event.BaseLocalEvent;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.handler.AbstractLocalEventHandler;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.processor.service.intf.LocalEventProcessorService;

/**
 *
 * @author Niels
 * @date 2017/11/3
 */
public class LocalEventProcessorServiceImpl implements LocalEventProcessorService {

    private static final LocalEventProcessorServiceImpl INSTANCE = new LocalEventProcessorServiceImpl();
    private final ProcessorManager processorManager;

    private LocalEventProcessorServiceImpl() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_LOCAL);
    }

    public static LocalEventProcessorService getInstance() {
        return INSTANCE;
    }

    @Override
    public void dispatch(BaseLocalEvent event) {
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

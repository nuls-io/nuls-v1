package io.nuls.event.bus.processor.service.impl;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.event.handler.AbstractLocalNulsEventHandler;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.processor.service.intf.LocalProcessorService;

/**
 *
 * @author Niels
 * @date 2017/11/3
 */
public class LocalProcessorServiceImpl implements LocalProcessorService {

    private static final LocalProcessorServiceImpl INSTANCE = new LocalProcessorServiceImpl();
    private final ProcessorManager processorManager;

    private LocalProcessorServiceImpl() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_LOCAL);
    }

    public static LocalProcessorService getInstance() {
        return INSTANCE;
    }

    @Override
    public void send(BaseNulsEvent event) {
        processorManager.offer(event);
    }

    @Override
    public String registerEventHandler(Class<? extends BaseNulsEvent> eventClass, AbstractLocalNulsEventHandler<? extends BaseNulsEvent> handler) {
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

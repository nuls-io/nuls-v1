package io.nuls.event.bus.processor.service.impl;

import io.nuls.core.event.NulsEvent;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.event.handler.LocalNulsEventHandler;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.processor.service.intf.LocalProcessorService;

/**
 * Created by Niels on 2017/11/3.
 */
public class LocalProcessorServiceImpl implements LocalProcessorService {

    private static final LocalProcessorServiceImpl instance = new LocalProcessorServiceImpl();
    private final ProcessorManager processorManager;

    private LocalProcessorServiceImpl() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_LOCAL);
    }

    public static LocalProcessorService getInstance() {
        return instance;
    }

    @Override
    public void send(NulsEvent event) {
        processorManager.offer(event);
    }

    @Override
    public String registerEventHandler(Class<? extends NulsEvent> eventClass, LocalNulsEventHandler<? extends NulsEvent> handler) {
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

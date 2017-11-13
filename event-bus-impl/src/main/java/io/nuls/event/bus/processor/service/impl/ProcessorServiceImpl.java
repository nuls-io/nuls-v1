package io.nuls.event.bus.processor.service.impl;

import io.nuls.core.event.NulsEvent;
import io.nuls.event.bus.event.handler.NulsEventHandler;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.processor.service.intf.ProcessorService;

/**
 * Created by Niels on 2017/11/3.
 * nuls.io
 */
public class ProcessorServiceImpl implements ProcessorService {

    private static final ProcessorServiceImpl processorManager = new ProcessorServiceImpl();

    private ProcessorServiceImpl() {
    }

    public static ProcessorService getInstance() {
        return processorManager;
    }

    public void send(NulsEvent event) {
        ProcessorManager.offer(event);
    }

    @Override
    public String registerEventHandler(Class<? extends NulsEvent> eventClass, NulsEventHandler<? extends NulsEvent> handler) {
        return ProcessorManager.registerEventHandler(eventClass,handler);
    }

    public void removeEventHandler(String handlerId) {
        ProcessorManager.removeEventHandler(handlerId);
    }
}

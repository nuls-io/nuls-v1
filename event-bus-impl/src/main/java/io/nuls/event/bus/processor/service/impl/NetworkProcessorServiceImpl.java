package io.nuls.event.bus.processor.service.impl;

import io.nuls.core.event.NulsEvent;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;

/**
 * Created by Niels on 2017/11/3.
 */
public class NetworkProcessorServiceImpl implements NetworkProcessorService {

    private static final NetworkProcessorServiceImpl instance = new NetworkProcessorServiceImpl();
    private final ProcessorManager processorManager;

    private NetworkProcessorServiceImpl() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_NETWORK);
    }

    public static NetworkProcessorServiceImpl getInstance() {
        return instance;
    }

    public void send(NulsEvent event) {
        processorManager.offer(event);
    }

    @Override
    public String registerEventHandler(Class<? extends NulsEvent> eventClass, NetworkNulsEventHandler<? extends NulsEvent> handler) {
        return processorManager.registerEventHandler(eventClass, handler);
    }

    public void removeEventHandler(String handlerId) {
        processorManager.removeEventHandler(handlerId);
    }

    @Override
    public void shutdown() {
        processorManager.shutdown();
    }
}

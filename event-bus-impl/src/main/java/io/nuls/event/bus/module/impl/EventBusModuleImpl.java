package io.nuls.event.bus.module.impl;

import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.module.intf.AbstractEventBusModule;
import io.nuls.event.bus.processor.service.impl.LocalProcessorServiceImpl;
import io.nuls.event.bus.processor.service.impl.NetworkProcessorServiceImpl;
import io.nuls.event.bus.processor.service.intf.LocalProcessorService;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;

/**
 * Created by Niels on 2017/11/6.
 */
public class EventBusModuleImpl extends AbstractEventBusModule {

    private LocalProcessorService localService;
    private NetworkProcessorService networkService;

    public EventBusModuleImpl() {
        super();
    }

    @Override
    public void start() {
        localService = LocalProcessorServiceImpl.getInstance();
        networkService = NetworkProcessorServiceImpl.getInstance();
        this.registerService(localService);
        this.registerService(networkService);
    }

    @Override
    public void shutdown() {
        localService.shutdown();
        networkService.shutdown();
    }

    @Override
    public void destroy() {
        localService.shutdown();
        networkService.shutdown();
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public int getVersion() {
        return EventBusConstant.EVENT_BUS_MODULE_VERSION;
    }

}

package io.nuls.event.bus.module.impl;

import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.constant.EventConstant;
import io.nuls.event.bus.event.CommonHashEvent;
import io.nuls.event.bus.event.GetBodyEvent;
import io.nuls.event.bus.handler.EventHashHandler;
import io.nuls.event.bus.handler.GetEventBodyHandler;
import io.nuls.event.bus.module.intf.AbstractEventBusModule;
import io.nuls.event.bus.processor.service.impl.LocalProcessorServiceImpl;
import io.nuls.event.bus.processor.service.impl.NetworkProcessorServiceImpl;
import io.nuls.event.bus.processor.service.intf.LocalProcessorService;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;
import io.nuls.event.bus.service.impl.EventServiceImpl;

/**
 * @author Niels
 * @date 2017/11/6
 */
public class EventBusModuleImpl extends AbstractEventBusModule {

    private LocalProcessorService localService;
    private NetworkProcessorService networkService;

    public EventBusModuleImpl() {
        super();
        this.registerEvent(EventConstant.EVENT_TYPE_COMMON_EVENT_HASH_EVENT, CommonHashEvent.class);
        this.registerEvent(EventConstant.EVENT_TYPE_GET_EVENT_BODY_EVENT, GetBodyEvent.class);
    }

    @Override
    public void start() {
        localService = LocalProcessorServiceImpl.getInstance();
        networkService = NetworkProcessorServiceImpl.getInstance();
        networkService.registerEventHandler(CommonHashEvent.class, new EventHashHandler());
        networkService.registerEventHandler(GetBodyEvent.class, new GetEventBodyHandler());
        this.registerService(localService);
        this.registerService(networkService);
        this.registerService(EventServiceImpl.getInstance());
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
        //todo
        return null;
    }

    @Override
    public int getVersion() {
        return EventBusConstant.EVENT_BUS_MODULE_VERSION;
    }

}

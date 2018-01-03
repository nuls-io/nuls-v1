package io.nuls.event.bus.module.impl;

import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.constant.EventConstant;
import io.nuls.event.bus.event.CommonDigestEvent;
import io.nuls.event.bus.event.GetEventBodyEvent;
import io.nuls.event.bus.service.impl.EventProducerImpl;
import io.nuls.event.bus.service.intf.EventProducer;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.event.bus.handler.CommonDigestHandler;
import io.nuls.event.bus.handler.GetEventBodyHandler;
import io.nuls.event.bus.handler.ReplyNoticeHandler;
import io.nuls.event.bus.module.intf.AbstractEventBusModule;
import io.nuls.event.bus.processor.service.impl.LocalEventProcessorServiceImpl;
import io.nuls.event.bus.processor.service.impl.NetworkEventProcessorServiceImpl;
import io.nuls.event.bus.processor.service.intf.LocalEventProcessorService;
import io.nuls.event.bus.processor.service.intf.NetworkEventProcessorService;
import io.nuls.event.bus.service.impl.NetworkEventBroadcasterImpl;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.network.message.ReplyNotice;

/**
 * @author Niels
 * @date 2017/11/6
 */
public class EventBusModuleImpl extends AbstractEventBusModule {

    private LocalEventProcessorService localService;
    private NetworkEventProcessorService networkService;

    public EventBusModuleImpl() {
        super();
        this.publish(EventConstant.EVENT_TYPE_COMMON_EVENT_HASH_EVENT, CommonDigestEvent.class);
        this.publish(EventConstant.EVENT_TYPE_GET_EVENT_BODY_EVENT, GetEventBodyEvent.class);
    }

    @Override
    public void init() {
        EventCacheService.getInstance().init();
    }

    @Override
    public void start() {
        localService = LocalEventProcessorServiceImpl.getInstance();
        networkService = NetworkEventProcessorServiceImpl.getInstance();
        networkService.registerEventHandler(CommonDigestEvent.class, new CommonDigestHandler());
        networkService.registerEventHandler(GetEventBodyEvent.class, new GetEventBodyHandler());
        this.registerService(localService);
        this.registerService(networkService);
        this.registerService(NetworkEventBroadcaster.class, NetworkEventBroadcasterImpl.getInstance());
        this.registerService(EventProducer.class, EventProducerImpl.getInstance());
        ReplyNoticeHandler replyNoticeHandler = new ReplyNoticeHandler();
        this.localService.registerEventHandler(ReplyNotice.class, replyNoticeHandler);

    }

    @Override
    public void shutdown() {
        localService.shutdown();
        networkService.shutdown();
    }

    @Override
    public void destroy() {
        EventCacheService.getInstance().destroy();
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

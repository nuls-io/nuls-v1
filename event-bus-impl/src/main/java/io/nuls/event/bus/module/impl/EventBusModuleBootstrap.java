package io.nuls.event.bus.module.impl;

import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.constant.EventConstant;
import io.nuls.event.bus.event.CommonDigestEvent;
import io.nuls.event.bus.event.GetEventBodyEvent;
import io.nuls.event.bus.service.impl.*;
import io.nuls.event.bus.service.intf.*;
import io.nuls.event.bus.handler.CommonDigestHandler;
import io.nuls.event.bus.handler.GetEventBodyHandler;
import io.nuls.event.bus.handler.ReplyNoticeHandler;
import io.nuls.event.bus.module.intf.AbstractEventBusModule;
import io.nuls.network.message.ReplyNotice;

/**
 * @author Niels
 * @date 2017/11/6
 */
public class EventBusModuleBootstrap extends AbstractEventBusModule {

    private EventProducer producer;
    private EventConsumer consumer;

    public EventBusModuleBootstrap() {
        super();
    }

    @Override
    public void init() {
        EventCacheService.getInstance().init();
    }

    @Override
    public void start() {
        producer = EventProducerImpl.getInstance();
        consumer = EventConsumerImpl.getInstance();
        consumer.subscribeNetworkEvent(CommonDigestEvent.class, new CommonDigestHandler());
        consumer.subscribeNetworkEvent(GetEventBodyEvent.class, new GetEventBodyHandler());
        this.registerService(producer);
        this.registerService(consumer);
        this.registerService(NetworkEventBroadcaster.class, NetworkEventBroadcasterImpl.getInstance());
        this.registerService(EventProducer.class, EventProducerImpl.getInstance());
        ReplyNoticeHandler replyNoticeHandler = new ReplyNoticeHandler();
        this.consumer.subscribeLocalEvent(ReplyNotice.class, replyNoticeHandler);

    }

    @Override
    public void shutdown() {
        LocalEventService.getInstance().shutdown();
        NetworkEventService.getInstance().shutdown();
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

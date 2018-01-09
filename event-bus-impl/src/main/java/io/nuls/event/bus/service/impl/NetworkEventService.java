package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.processor.manager.ProcessorManager;

/**
 * @author Niels
 * @date 2017/11/3
 */
public class NetworkEventService {

    private static final NetworkEventService INSTANCE = new NetworkEventService();
    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private final ProcessorManager processorManager;

    private NetworkEventService() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_NETWORK);
    }

    public static NetworkEventService getInstance() {
        return INSTANCE;
    }

    public void publish(byte[] event, String nodeId) {
        try {
            BaseEvent eventObject = EventManager.getInstance(event);
            this.publish(eventObject, nodeId);
        } catch (NulsException e) {
            Log.error(e);
        }

    }

    public void publish(BaseEvent event, String nodeId) {
        boolean exist = eventCacheService.isKnown(event.getHash().getDigestHex());
        if (exist) {
            return;
        }
        eventCacheService.cacheRecievedEventHash(event.getHash().getDigestHex());
        processorManager.offer(new ProcessData(event, nodeId));

    }

    public String registerEventHandler(Class<? extends BaseEvent> eventClass, AbstractEventHandler<? extends BaseEvent> handler) {
        return registerEventHandler(null,eventClass, handler);
    }

    public void removeEventHandler(String handlerId) {
        processorManager.removeEventHandler(handlerId);
    }

    public void shutdown() {
        processorManager.shutdown();
    }

    public String registerEventHandler(String id, Class<? extends BaseEvent> eventClass, AbstractEventHandler<? extends BaseEvent> eventHandler) {
       return processorManager.registerEventHandler(id,eventClass,eventHandler);

    }
}

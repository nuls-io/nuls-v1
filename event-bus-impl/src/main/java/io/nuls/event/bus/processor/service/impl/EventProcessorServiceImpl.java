package io.nuls.event.bus.processor.service.impl;

import io.nuls.core.bus.BusDataManager;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.processor.service.intf.EventProcessorService;
import io.nuls.event.bus.service.impl.EventCacheService;

/**
 *
 * @author Niels
 * @date 2017/11/3
 */
public class EventProcessorServiceImpl implements EventProcessorService {

    private static final EventProcessorServiceImpl INSTANCE = new EventProcessorServiceImpl();
    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private final ProcessorManager processorManager;

    private EventProcessorServiceImpl() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_NETWORK);
    }

    public static EventProcessorServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void send(byte[] event,String peerId) {
        try {
            BaseNulsEvent eventObject = BusDataManager.getEventInstance(event);
            eventObject.parse(new NulsByteBuffer(event));
            boolean exist = eventCacheService.isKnown(eventObject.getHash().getDigestHex());
            if (exist) {
                return;
            }
            eventCacheService.cacheRecievedEventHash(eventObject.getHash().getDigestHex());
            processorManager.offer(new ProcessData(eventObject,peerId));
        } catch (IllegalAccessException e) {
            Log.error(e);
        } catch (InstantiationException e) {
            Log.error(e);
        }
    }

    @Override
    public String registerEventHandler(Class<? extends BaseNulsEvent> eventClass, AbstractEventBusHandler<? extends BaseNulsEvent> handler) {
        return processorManager.registerEventHandler(eventClass, handler);
    }

    @Override
    public void removeEventHandler(String handlerId) {
        processorManager.removeEventHandler(handlerId);
    }

    @Override
    public void shutdown() {
        processorManager.shutdown();
    }
}

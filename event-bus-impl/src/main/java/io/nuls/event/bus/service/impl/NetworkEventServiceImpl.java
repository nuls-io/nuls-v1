package io.nuls.event.bus.service.impl;

import io.nuls.core.event.EventManager;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.service.intf.NetworkEventService;

/**
 * @author Niels
 * @date 2017/11/3
 */
public class NetworkEventServiceImpl implements NetworkEventService {

    private static final NetworkEventServiceImpl INSTANCE = new NetworkEventServiceImpl();
    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private final ProcessorManager processorManager;

    private NetworkEventServiceImpl() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_NETWORK);
    }

    public static NetworkEventServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void publish(byte[] event, String peerId) {
        try {
            BaseNetworkEvent eventObject = EventManager.getNetworkEventInstance(event);
            this.publish(eventObject, peerId);
        } catch (IllegalAccessException e) {
            Log.error(e);
        } catch (InstantiationException e) {
            Log.error(e);
        } catch (NulsException e) {
            Log.error(e);
        }

    }

    @Override
    public void publish(BaseNetworkEvent event, String peerId) {
        boolean exist = eventCacheService.isKnown(event.getHash().getDigestHex());
        if (exist) {
            return;
        }
        eventCacheService.cacheRecievedEventHash(event.getHash().getDigestHex());
        processorManager.offer(new ProcessData(event, peerId));

    }

    @Override
    public String registerEventHandler(Class<? extends BaseNetworkEvent> eventClass, AbstractNetworkEventHandler<? extends BaseNetworkEvent> handler) {
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

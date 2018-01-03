package io.nuls.event.bus.processor.service.impl;

import io.nuls.core.event.EventManager;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.processor.service.intf.NetworkEventProcessorService;
import io.nuls.event.bus.service.impl.EventCacheService;

/**
 * @author Niels
 * @date 2017/11/3
 */
public class NetworkEventProcessorServiceImpl implements NetworkEventProcessorService {

    private static final NetworkEventProcessorServiceImpl INSTANCE = new NetworkEventProcessorServiceImpl();
    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private final ProcessorManager processorManager;

    private NetworkEventProcessorServiceImpl() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_NETWORK);
    }

    public static NetworkEventProcessorServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void dispatch(byte[] event, String peerId) {
        try {
            BaseNetworkEvent eventObject = EventManager.getNetworkEventInstance(event);
            this.dispatch(eventObject, peerId);
        } catch (IllegalAccessException e) {
            Log.error(e);
        } catch (InstantiationException e) {
            Log.error(e);
        } catch (NulsException e) {
            Log.error(e);
        }

    }

    @Override
    public void dispatch(BaseNetworkEvent event, String peerId) {
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

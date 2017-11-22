package io.nuls.event.bus.processor.service.impl;

import io.nuls.core.event.EventManager;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;

/**
 *
 * @author Niels
 * @date 2017/11/3
 */
public class NetworkProcessorServiceImpl implements NetworkProcessorService {

    private static final NetworkProcessorServiceImpl INSTANCE = new NetworkProcessorServiceImpl();
    private final ProcessorManager processorManager;

    private NetworkProcessorServiceImpl() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_NETWORK);
    }

    public static NetworkProcessorServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void send(byte[] event) {
        NulsEventHeader header = new NulsEventHeader();
        header.parse(new NulsByteBuffer(event));
        try {
            BaseNulsEvent eventObject = EventManager.getInstanceByHeader(header);
            eventObject.parse(new NulsByteBuffer(event));
            processorManager.offer(eventObject);
        } catch (IllegalAccessException e) {
            Log.error(e);
        } catch (InstantiationException e) {
            Log.error(e);
        }
    }

    @Override
    public String registerEventHandler(Class<? extends BaseNulsEvent> eventClass, AbstractNetworkNulsEventHandler<? extends BaseNulsEvent> handler) {
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

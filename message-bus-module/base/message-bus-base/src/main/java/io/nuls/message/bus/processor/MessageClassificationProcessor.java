package io.nuls.message.bus.processor;

import com.lmax.disruptor.EventHandler;
import io.nuls.core.tools.disruptor.DisruptorData;
import io.nuls.core.tools.log.Log;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.manager.HandlerManager;
import io.nuls.message.bus.model.ProcessData;
import io.nuls.message.bus.processor.thread.NulsMessageCall;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ln on 2018-05-23.
 */
public class MessageClassificationProcessor<E extends BaseMessage> implements EventHandler<DisruptorData<ProcessData<E>>> {

    private HandlerManager handlerManager = HandlerManager.getInstance();
    private Map<Class<? extends BaseMessage>, ExecutorService> handlerService = new HashMap<>();

    @Override
    public void onEvent(DisruptorData<ProcessData<E>> disruptorData, long l, boolean b) throws Exception {

        if (null == disruptorData || disruptorData.getData() == null) {
            Log.warn("there is null data in disruptorData!");
            return;
        }
        if (disruptorData.isStoped()) {
            return;
        }

        ProcessData processData = disruptorData.getData();
        Class<? extends BaseMessage> serviceId = processData.getData().getClass();
        Set<NulsMessageHandler> handlers = handlerManager.getHandlerList(serviceId);
        ExecutorService handlerExecutor = handlerService.get(serviceId);
        if(handlerExecutor == null) {
            handlerExecutor = Executors.newSingleThreadExecutor();
            handlerService.put(serviceId, handlerExecutor);
        }
        for(NulsMessageHandler handler : handlers) {
            handlerExecutor.execute(new NulsMessageCall(processData, handler));
        }
    }

    public void shutdown() {
        if(handlerService == null) {
            return;
        }
        for (Map.Entry<Class<? extends BaseMessage>, ExecutorService> entry: handlerService.entrySet()) {
            entry.getValue().shutdown();
        }
    }
}

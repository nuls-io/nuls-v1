package io.nuls.event.bus.processor.thread;

import com.lmax.disruptor.WorkHandler;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.NulsEvent;
import io.nuls.core.thread.NulsThread;
import io.nuls.event.bus.module.impl.EventBusModuleImpl;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.utils.disruptor.DisruptorEvent;

/**
 * Created by Niels on 2017/11/6.
 */
public class EventBusDispatchThread extends NulsThread implements WorkHandler<DisruptorEvent<NulsEvent>> {

    private final ProcessorManager processorManager;

    public EventBusDispatchThread(String threadName, ProcessorManager processorManager) {
        super(NulsContext.getInstance().getModule(EventBusModuleImpl.class), threadName);
        this.processorManager = processorManager;
    }

    @Override
    public void onEvent(DisruptorEvent<NulsEvent> event) throws Exception {
        if (null == event) {
            return;
        }
        processorManager.executeHandlers(event.getData());
    }

    @Override
    public void run() {
        //do nothing
        return;
    }
}

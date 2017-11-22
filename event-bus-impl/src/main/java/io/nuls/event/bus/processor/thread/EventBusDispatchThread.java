package io.nuls.event.bus.processor.thread;

import com.lmax.disruptor.WorkHandler;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.thread.BaseNulsThread;
import io.nuls.event.bus.module.impl.EventBusModuleImpl;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.utils.disruptor.DisruptorEvent;

/**
 * Created by Niels on 2017/11/6.
 */
public class EventBusDispatchThread extends BaseNulsThread implements WorkHandler<DisruptorEvent<BaseNulsEvent>> {

    private final ProcessorManager processorManager;

    public EventBusDispatchThread(String threadName, ProcessorManager processorManager) {
        super(NulsContext.getInstance().getModule(EventBusModuleImpl.class), threadName);
        this.processorManager = processorManager;
    }

    @Override
    public void onEvent(DisruptorEvent<BaseNulsEvent> event) throws Exception {
        if (null == event) {
            System.out.println("did sth ....");
            return;
        }
        processorManager.executeHandlers(event.getData());
    }

    @Override
    public void run() {
        //do nothing
        System.out.println("do sth.....");

        return;
    }
}

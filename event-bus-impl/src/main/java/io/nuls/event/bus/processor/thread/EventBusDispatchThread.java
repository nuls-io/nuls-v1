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
 * nuls.io
 */
public class  EventBusDispatchThread extends NulsThread implements WorkHandler<DisruptorEvent<NulsEvent>> {

    public EventBusDispatchThread(String threadName) {
        super(NulsContext.getInstance().getModule(EventBusModuleImpl.class), threadName);
    }

    @Override
    public void onEvent(DisruptorEvent<NulsEvent> event) throws Exception {
        if (null == event) {
            return;
        }
        ProcessorManager.executeHandlers(event.getData());
    }

    @Override
    public void run() {
        //do nothing
        return;
    }
}

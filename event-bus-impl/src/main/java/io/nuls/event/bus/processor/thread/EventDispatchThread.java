package io.nuls.event.bus.processor.thread;

import com.lmax.disruptor.WorkHandler;
import io.nuls.core.thread.BaseThread;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.utils.disruptor.DisruptorEvent;

/**
 * @author Niels
 * @date 2017/11/6
 */
public class EventDispatchThread extends BaseThread implements WorkHandler<DisruptorEvent<ProcessData>> {

    private final ProcessorManager processorManager;

    public EventDispatchThread(ProcessorManager processorManager) {
        this.processorManager = processorManager;
    }

    @Override
    public void onEvent(DisruptorEvent<ProcessData > event) throws Exception {
        if (null == event) {
            Log.debug("did sth ....");
            return;
        }
        processorManager.executeHandlers(event.getData());
    }
}

package io.nuls.message.bus.processor.thread;

import com.lmax.disruptor.WorkHandler;
import io.nuls.core.tools.disruptor.DisruptorData;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.thread.BaseThread;
import io.nuls.message.bus.processor.manager.ProcessData;
import io.nuls.message.bus.processor.manager.ProcessorManager;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class MessageDispatchThread extends BaseThread implements WorkHandler<DisruptorData<ProcessData>> {

    private final ProcessorManager processorManager;

    public MessageDispatchThread(ProcessorManager processorManager) {
        this.processorManager = processorManager;
    }

    @Override
    public void onEvent(DisruptorData<ProcessData> disruptorData) throws Exception {
        if (null == disruptorData || disruptorData.getData() == null || disruptorData.isStoped()) {
            Log.warn("there is null data in disruptorData!");
            return;
        }
        try {
            processorManager.executeHandlers(disruptorData.getData());
        } catch (Exception e) {
            Log.error(e);
        }
    }
}

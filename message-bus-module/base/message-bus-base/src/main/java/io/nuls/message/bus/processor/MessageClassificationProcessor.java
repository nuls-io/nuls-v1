package io.nuls.message.bus.processor;

import com.lmax.disruptor.EventHandler;
import io.nuls.core.tools.disruptor.DisruptorData;
import io.nuls.message.bus.processor.manager.ProcessData;
import io.nuls.message.bus.processor.manager.ProcessorManager;
import io.nuls.protocol.message.SmallBlockMessage;
import io.nuls.protocol.message.TransactionMessage;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * Created by ln on 2018-05-23.
 */
public class MessageClassificationProcessor<E extends BaseMessage> implements EventHandler<DisruptorData<ProcessData<E>>> {

    private final ProcessorManager processorManager;

    public MessageClassificationProcessor(ProcessorManager processorManager) {
        this.processorManager = processorManager;
    }

    @Override
    public void onEvent(DisruptorData<ProcessData<E>> processDataDisruptorData, long l, boolean b) throws Exception {
        BaseMessage message = processDataDisruptorData.getData().getData();
        if(message instanceof TransactionMessage || message instanceof SmallBlockMessage) {
            processorManager.executeHandlers(processDataDisruptorData.getData());
            processDataDisruptorData.setStoped(true);
        }
    }
}

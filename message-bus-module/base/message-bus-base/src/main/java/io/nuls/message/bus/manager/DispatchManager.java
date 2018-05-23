package io.nuls.message.bus.manager;

import io.nuls.core.tools.disruptor.DisruptorData;
import io.nuls.core.tools.disruptor.DisruptorUtil;
import io.nuls.kernel.module.service.ModuleService;
import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.message.bus.module.MessageBusModuleBootstrap;
import io.nuls.message.bus.processor.MessageFilterProcessor;
import io.nuls.message.bus.processor.MessageClassificationProcessor;
import io.nuls.message.bus.model.ProcessData;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * 消息处理的管理器
 * Message processing manager.
 *
 * @author: Charlie
 * @date: 2018/5/6
 */
public class DispatchManager<M extends BaseMessage> {

    private static final DispatchManager INSTANCE = new DispatchManager();

    private DisruptorUtil<DisruptorData<ProcessData<M>>> disruptorService = DisruptorUtil.getInstance();
    private String disruptorName = MessageBusConstant.DISRUPTOR_NAME;
    private MessageClassificationProcessor messageProcesser;

    public static DispatchManager getInstance() {
        return INSTANCE;
    }

    private DispatchManager() {
    }

    public final void init(boolean messageChecking) {
        NulsThreadFactory nulsThreadFactory = new NulsThreadFactory(ModuleService.getInstance().getModuleId(MessageBusModuleBootstrap.class), disruptorName);
        disruptorService.createDisruptor(disruptorName, MessageBusConstant.DEFAULT_RING_BUFFER_SIZE, nulsThreadFactory);

        messageProcesser = new MessageClassificationProcessor();
        disruptorService.handleEventWith(disruptorName, new MessageFilterProcessor()).then(messageProcesser);

        disruptorService.start(disruptorName);
    }

    public void shutdown() {
        messageProcesser.shutdown();
        disruptorService.shutdown(disruptorName);
    }

    public void offer(ProcessData<M> data) {
        disruptorService.offer(disruptorName, data);
    }
}

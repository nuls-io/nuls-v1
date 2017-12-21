package io.nuls.event.bus.processor.service.impl;

import io.nuls.core.notice.BaseNulsNotice;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.bus.handler.AbstractNoticeBusHandler;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.processor.service.intf.NoticeProcessorService;

/**
 *
 * @author Niels
 * @date 2017/11/3
 */
public class NoticeProcessorServiceImpl implements NoticeProcessorService {

    private static final NoticeProcessorServiceImpl INSTANCE = new NoticeProcessorServiceImpl();
    private final ProcessorManager processorManager;

    private NoticeProcessorServiceImpl() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_LOCAL);
    }

    public static NoticeProcessorService getInstance() {
        return INSTANCE;
    }

    @Override
    public void notice(BaseNulsNotice event) {
        processorManager.offer(new ProcessData(event));
    }

    @Override
    public String registerNoticeHandler(Class<? extends BaseNulsNotice> eventClass, AbstractNoticeBusHandler<? extends BaseNulsNotice> handler) {
        return processorManager.registerEventHandler(eventClass, handler);
    }

    @Override
    public void removeNoticeHandler(String handlerId) {
        processorManager.removeEventHandler(handlerId);
    }

    @Override
    public void shutdown() {
        this.processorManager.shutdown();
    }
}

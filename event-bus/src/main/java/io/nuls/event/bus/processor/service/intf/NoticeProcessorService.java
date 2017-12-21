package io.nuls.event.bus.processor.service.intf;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.notice.BaseNulsNotice;
import io.nuls.event.bus.event.handler.AbstractNoticeHandler;

/**
 * @author Niels
 * @date 2017/11/6
 */
public interface NoticeProcessorService {

    public void notice(BaseNulsNotice data);

    public String registerNoticeHandler(Class<? extends BaseNulsNotice> eventClass, AbstractNoticeHandler<? extends BaseNulsNotice> handler);

    public void removeNoticeHandler(String handlerId);

    void shutdown();
}

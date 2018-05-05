package io.nuls.event.bus.handler.intf;

import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;
import io.nuls.kernel.exception.NulsException;
import io.nuls.protocol.event.base.BaseEvent;

/**
 * Nuls 事件处理器接口
 * The Nuls event handler interface.
 * @author: Charlie
 * @date: 2018/5/5
 */
public interface NulsEventHandler<T extends BaseEvent> {

    /**
     * 添加一个过滤器
     * add a filter
     * @param filter 
     */
    void addFilter(NulsEventFilter<T> filter);

    NulsEventFilterChain getFilterChian();

    void onEvent(T event, String formId) throws NulsException;
}

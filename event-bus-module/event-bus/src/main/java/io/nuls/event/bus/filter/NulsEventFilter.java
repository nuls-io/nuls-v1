package io.nuls.event.bus.filter;

import io.nuls.protocol.event.base.BaseEvent;

/**
 * Nuls事件过滤器
 * The Nuls event filter.
 * @author: Charlie
 * @date: 2018/5/5
 */
public interface NulsEventFilter<T extends BaseEvent> {

    void doFilter(T data, NulsEventFilterChain chain);
}

package io.nuls.event.bus.filter;

import io.nuls.core.event.BaseEvent;

/**
 * @author Niels
 * @date 2017/11/7
 */
public interface NulsEventFilter<T extends BaseEvent> {

    void doFilter(T data, NulsEventFilterChain chain);

}

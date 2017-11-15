package io.nuls.event.bus.event.filter;

import io.nuls.core.event.NulsEvent;

/**
 * Created by Niels on 2017/11/7.
 *
 */
public interface NulsEventFilter<T extends NulsEvent> {

    void doFilter(T event,NulsEventFilterChain chain);

}

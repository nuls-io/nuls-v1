package io.nuls.event.bus.event.handler.intf;

import io.nuls.core.event.NulsEvent;
import io.nuls.event.bus.event.filter.NulsEventFilter;
import io.nuls.event.bus.event.filter.NulsEventFilterChain;

/**
 * Created by Niels on 2017/11/15.
 */
public interface NulsEventHandler<T extends NulsEvent> {

    void addFilter(NulsEventFilter<T> filter);

    NulsEventFilterChain getFilterChain();

    void onEvent(T event);
}

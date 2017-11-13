package io.nuls.event.bus.event.handler;

import io.nuls.core.event.NulsEvent;
import io.nuls.event.bus.event.filter.NulsEventFilter;
import io.nuls.event.bus.event.filter.NulsEventFilterChain;

/**
 * Created by Niels on 2017/11/6.
 * nuls.io
 */
public abstract class NulsEventHandler<T extends NulsEvent> {

    private NulsEventFilterChain filterChain = new NulsEventFilterChain();

    public void addFilter(NulsEventFilter<? extends NulsEvent> filter) {
        filterChain.addFilter(filter);
    }

    public abstract void onEvent(T event);

    public NulsEventFilterChain getFilterChain() {
        return filterChain;
    }
}

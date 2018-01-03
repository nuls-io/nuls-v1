package io.nuls.event.bus.handler;

import io.nuls.core.event.BaseLocalEvent;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;
import io.nuls.event.bus.handler.intf.NulsEventHandler;

/**
 *
 * @author Niels
 * @date 2017/11/6
 */
public abstract class AbstractLocalEventHandler<T extends BaseLocalEvent> implements NulsEventHandler<T> {

    private NulsEventFilterChain filterChain = new NulsEventFilterChain();

    @Override
    public void addFilter(NulsEventFilter<T> filter) {
        filterChain.addFilter(filter);
    }

    @Override
    public NulsEventFilterChain getFilterChain() {
        return filterChain;
    }
}

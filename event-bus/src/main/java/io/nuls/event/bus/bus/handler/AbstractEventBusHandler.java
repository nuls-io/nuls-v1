package io.nuls.event.bus.bus.handler;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;
import io.nuls.event.bus.bus.handler.intf.NulsBusHandler;

/**
 *
 * @author Niels
 * @date 2017/11/6
 *
 */
public abstract class AbstractEventBusHandler<T extends BaseNulsEvent>  implements NulsBusHandler<T> {

    private NulsBusFilterChain filterChain = new NulsBusFilterChain();

    @Override
    public void addFilter(NulsBusFilter<T> filter) {
        filterChain.addFilter(filter);
    }

    @Override
    public NulsBusFilterChain getFilterChain() {
        return filterChain;
    }
}

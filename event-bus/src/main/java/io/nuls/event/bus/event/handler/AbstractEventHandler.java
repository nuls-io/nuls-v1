package io.nuls.event.bus.event.handler;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;
import io.nuls.event.bus.event.handler.intf.NulsHandler;

/**
 *
 * @author Niels
 * @date 2017/11/6
 *
 */
public abstract class AbstractEventHandler<T extends BaseNulsEvent>  implements NulsHandler<T> {

    private NulsFilterChain filterChain = new NulsFilterChain();

    @Override
    public void addFilter(NulsFilter<T> filter) {
        filterChain.addFilter(filter);
    }

    @Override
    public NulsFilterChain getFilterChain() {
        return filterChain;
    }
}

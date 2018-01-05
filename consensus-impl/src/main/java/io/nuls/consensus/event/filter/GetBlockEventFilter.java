package io.nuls.consensus.event.filter;

import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class GetBlockEventFilter implements NulsEventFilter<GetBlockEvent> {
    @Override
    public void doFilter(GetBlockEvent event, NulsEventFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

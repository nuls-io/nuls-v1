package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class GetBlockFilter implements NulsFilter<GetBlockEvent> {
    @Override
    public void doFilter(GetBlockEvent event, NulsFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

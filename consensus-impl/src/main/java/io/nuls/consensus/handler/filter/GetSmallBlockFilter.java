package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.GetSmallBlockEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class GetSmallBlockFilter implements NulsFilter<GetSmallBlockEvent> {
    @Override
    public void doFilter(GetSmallBlockEvent event, NulsFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

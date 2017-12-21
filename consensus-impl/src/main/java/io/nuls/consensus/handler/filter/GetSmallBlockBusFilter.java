package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.GetSmallBlockEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class GetSmallBlockBusFilter implements NulsBusFilter<GetSmallBlockEvent> {
    @Override
    public void doFilter(GetSmallBlockEvent event, NulsBusFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

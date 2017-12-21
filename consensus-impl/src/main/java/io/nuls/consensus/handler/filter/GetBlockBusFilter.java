package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class GetBlockBusFilter implements NulsBusFilter<GetBlockEvent> {
    @Override
    public void doFilter(GetBlockEvent event, NulsBusFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

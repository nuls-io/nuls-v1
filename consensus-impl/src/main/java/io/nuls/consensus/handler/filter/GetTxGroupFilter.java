package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.GetTxGroupEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class GetTxGroupFilter implements NulsBusFilter<GetTxGroupEvent> {
    @Override
    public void doFilter(GetTxGroupEvent event, NulsBusFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

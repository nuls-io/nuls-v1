package io.nuls.consensus.event.filter;

import io.nuls.consensus.event.GetTxGroupEvent;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class GetTxGroupFilter implements NulsEventFilter<GetTxGroupEvent> {
    @Override
    public void doFilter(GetTxGroupEvent event, NulsEventFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

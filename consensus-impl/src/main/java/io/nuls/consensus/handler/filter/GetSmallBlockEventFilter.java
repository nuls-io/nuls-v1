package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.GetSmallBlockEvent;
import io.nuls.event.bus.event.filter.NulsEventFilter;
import io.nuls.event.bus.event.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class GetSmallBlockEventFilter implements NulsEventFilter<GetSmallBlockEvent> {
    @Override
    public void doFilter(GetSmallBlockEvent event, NulsEventFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

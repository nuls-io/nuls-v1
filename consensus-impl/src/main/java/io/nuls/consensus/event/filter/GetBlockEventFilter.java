package io.nuls.consensus.event.filter;

import io.nuls.consensus.event.GetBlockRequest;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class GetBlockEventFilter implements NulsEventFilter<GetBlockRequest> {
    @Override
    public void doFilter(GetBlockRequest event, NulsEventFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

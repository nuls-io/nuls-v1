package io.nuls.consensus.event.filter;

import io.nuls.consensus.event.GetTxGroupRequest;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class GetTxGroupFilter implements NulsEventFilter<GetTxGroupRequest> {
    @Override
    public void doFilter(GetTxGroupRequest event, NulsEventFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

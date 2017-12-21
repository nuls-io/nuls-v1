package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class BlockHeaderBusFilter implements NulsBusFilter<BlockHeaderEvent> {
    @Override
    public void doFilter(BlockHeaderEvent event, NulsBusFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

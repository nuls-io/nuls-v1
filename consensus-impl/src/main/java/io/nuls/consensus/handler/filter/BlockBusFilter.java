package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class BlockBusFilter implements NulsBusFilter<BlockEvent> {

    public BlockBusFilter() {

    }

    @Override
    public void doFilter(BlockEvent event, NulsBusFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

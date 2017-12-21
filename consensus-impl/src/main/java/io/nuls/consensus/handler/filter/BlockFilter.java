package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class BlockFilter implements NulsFilter<BlockEvent> {

    public BlockFilter() {

    }

    @Override
    public void doFilter(BlockEvent event, NulsFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

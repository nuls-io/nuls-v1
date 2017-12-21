package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class BlockHeaderFilter implements NulsFilter<BlockHeaderEvent> {
    @Override
    public void doFilter(BlockHeaderEvent event, NulsFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

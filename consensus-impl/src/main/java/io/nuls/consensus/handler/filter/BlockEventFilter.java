package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.event.bus.event.filter.NulsEventFilter;
import io.nuls.event.bus.event.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class BlockEventFilter implements NulsEventFilter<BlockEvent> {


    private static BlockEventFilter filter = new BlockEventFilter();

    private BlockEventFilter() {

    }

    public static BlockEventFilter getInstance() {
        return filter;
    }

    @Override
    public void doFilter(BlockEvent event, NulsEventFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

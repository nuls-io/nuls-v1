package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.consensus.event.RedPunishConsensusEvent;
import io.nuls.event.bus.event.filter.NulsEventFilter;
import io.nuls.event.bus.event.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class RedPunishEventFilter implements NulsEventFilter<RedPunishConsensusEvent> {
    @Override
    public void doFilter(RedPunishConsensusEvent event, NulsEventFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

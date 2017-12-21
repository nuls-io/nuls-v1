package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.RedPunishConsensusEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class RedPunishFilter implements NulsFilter<RedPunishConsensusEvent> {
    @Override
    public void doFilter(RedPunishConsensusEvent event, NulsFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

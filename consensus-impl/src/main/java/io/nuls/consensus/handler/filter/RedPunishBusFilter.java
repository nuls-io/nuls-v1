package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.RedPunishConsensusEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class RedPunishBusFilter implements NulsBusFilter<RedPunishConsensusEvent> {
    @Override
    public void doFilter(RedPunishConsensusEvent event, NulsBusFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

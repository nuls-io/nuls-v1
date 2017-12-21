package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.YellowPunishConsensusEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class YellowPunishBusFilter implements NulsBusFilter<YellowPunishConsensusEvent> {
    @Override
    public void doFilter(YellowPunishConsensusEvent event, NulsBusFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

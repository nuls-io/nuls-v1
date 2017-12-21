package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.ExitConsensusEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ExitConsensusBusFilter implements NulsBusFilter<ExitConsensusEvent> {
    @Override
    public void doFilter(ExitConsensusEvent event, NulsBusFilterChain chain) {
        //todo


        chain.doFilter(event);
    }
}

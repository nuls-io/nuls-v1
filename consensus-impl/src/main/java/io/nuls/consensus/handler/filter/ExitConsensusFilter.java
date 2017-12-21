package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.ExitConsensusEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ExitConsensusFilter implements NulsFilter<ExitConsensusEvent> {
    @Override
    public void doFilter(ExitConsensusEvent event, NulsFilterChain chain) {
        //todo


        chain.doFilter(event);
    }
}

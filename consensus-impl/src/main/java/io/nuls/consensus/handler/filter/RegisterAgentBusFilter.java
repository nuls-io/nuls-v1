package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class RegisterAgentBusFilter implements NulsBusFilter<RegisterAgentEvent> {
    @Override
    public void doFilter(RegisterAgentEvent event, NulsBusFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}

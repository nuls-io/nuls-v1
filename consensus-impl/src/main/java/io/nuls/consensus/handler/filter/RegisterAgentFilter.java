package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class RegisterAgentFilter implements NulsFilter<RegisterAgentEvent> {
    @Override
    public void doFilter(RegisterAgentEvent event, NulsFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}
